import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {
  ClassAccessor,
  isDateProperty,
  isEnabled,
  isInSummary,
  isNumericProperty,
  isSearchable,
  Property, SelectionOption
} from "../../class-accessor";
import {MatTableDataSource, PageEvent, Sort} from "@angular/material";
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {SelectionModel} from "@angular/cdk/collections";
import {SelectionProvider} from "../crud.common";
import {AuthenticationService} from "../../security/authentication.service";
import {MediaObserver} from "@angular/flex-layout";
import {Observable, of, Subject, Subscription} from "rxjs";
import {ButtonInfo, getButtons} from "../../buttons";
import {Type} from "@angular/core/src/type";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'portofino-crud-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit, OnDestroy {

  @Input()
  classAccessor: ClassAccessor;
  @Input()
  selectionProviders: SelectionProvider[];
  @Input()
  sourceUrl: string;
  @Input()
  baseUrl: string;

  searchProperties: Property[] = [];
  form: FormGroup;
  results: SearchResults;
  resultsDataSource = new MatTableDataSource();
  resultFields: SearchResultField[] = [];
  isLoadingResults = false;
  datatableColumns: string[] = [];
  @Input()
  pageSize: number;
  sortInfo: Sort;
  page: number;

  @Input()
  selectionEnabled: boolean;
  @Input()
  selection = new SelectionModel<any>(true, []);
  readonly selectColumnName = "__select";
  @Input()
  refresh: Subject<void>;
  refreshSubscription: Subscription;

  @Input()
  parentButtons: ButtonInfo[] = [];
  @Input()
  parent: any;

  constructor(protected http: HttpClient, protected portofino: PortofinoService, protected translate: TranslateService,
              protected auth: AuthenticationService, public media: MediaObserver) {}

  ngOnInit() {
    this.setupForm();
    this.setupSelectionProviders();
    this.listenToMediaChanges();
    this.search();
    if(this.refresh) {
      this.refreshSubscription = this.refresh.subscribe(_ => this.refreshSearch())
    }
  }

  ngOnDestroy(): void {
    if(this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  protected setupForm() {
    const formControls = {};
    if (this.selectionEnabled) {
      this.datatableColumns.push(this.selectColumnName);
    }
    this.classAccessor.properties.forEach((property, i) => {
      property = Object.assign(new Property(), property);
      if (!isEnabled(property)) {
        return;
      }
      if (isSearchable(property)) {
        this.searchProperties.push(property);
        formControls[property.name] = new FormControl();
      }
      if (isInSummary(property)) {
        const field = new SearchResultField();
        field.name = property.name;
        field.key = property.key;
        field.inList = field.key || i < 3;
        field.label = property.label;
        if(property.kind == "blob") {
          field.href = row => {
            const value = row[property.name];
            if(value.value && value.value.size && value.value.size > 0) {
              return this.getBlobUrl(row.__rowKey, property.name);
            } else {
              return null;
            }
          };
          field.value = row => {
            const value = row[property.name];
            if(value.value && value.value.size && value.value.size > 0) {
              return of(value.displayValue);
            } else {
              return this.translate.get("Blob not found");
            }
          };
        }
        this.resultFields.push(field);
        this.datatableColumns.push(property.name);
      }
    });
    if (this.resultFields.length > 0 && !this.resultFields.some(f => f.key)) {
      this.resultFields[0].key = true;
    }
    this.resultFields.filter(f => f.key).forEach(f => {
      f.routerLink = row => this.baseUrl + '/' + row.__rowKey;
      this.resultFields[0].href = null;
    });
    this.customizeForm(formControls);
    this.form = new FormGroup(formControls);
  }

  protected customizeForm(formControls) {
    //Extension hook
  }

  protected setupSelectionProviders() {
    this.selectionProviders.forEach(sp => {
      sp.fieldNames.forEach((name, index) => {
        const property = this.searchProperties.find(p => p.name == name);
        if (!property) {
          return;
        }
        const spUrl = `${this.sourceUrl}/:selectionProvider/${sp.name}/${index}`;
        property.selectionProvider = {
          name: sp.name,
          index: index,
          displayMode: sp.searchDisplayMode,
          url: spUrl,
          nextProperty: null,
          updateDependentOptions: () => {
            const nextProperty = property.selectionProvider.nextProperty;
            if (nextProperty) {
              this.loadSelectionOptions(this.searchProperties.find(p => p.name == nextProperty));
            }
          },
          loadOptions: value => {
            this.loadSelectionOptions(property, value);
          },
          options: []
        };
        if (index < sp.fieldNames.length - 1) {
          property.selectionProvider.nextProperty = sp.fieldNames[index + 1];
        }
      });
    });
  }

  protected listenToMediaChanges() {
    let wasDatatableHidden = !this.isDataTable();
    this.media.media$.subscribe(() => {
      let refresh = false;
      if (this.isDataTable()) {
        refresh = wasDatatableHidden;
      } else {
        refresh = !wasDatatableHidden;
      }
      wasDatatableHidden = !this.isDataTable();
      if (refresh) {
        this.page = 0;
        this.refreshSearch();
      }
    });
  }

  protected loadSelectionOptions(property: Property, autocomplete: string = null) {
    const url = property.selectionProvider.url;
    let params = new HttpParams();
    if(property.selectionProvider.displayMode == 'AUTOCOMPLETE') {
      if(autocomplete) {
        params = params.set(`labelSearch`, autocomplete);
      } else {
        this.setSelectionOptions(property, []);
        return;
      }
    }
    this.http.get<SelectionOption[]>(url, { params: params }).subscribe(
      options => {
        this.setSelectionOptions(property, options);
        if(property.selectionProvider.displayMode == 'CHECKBOX') {
          const controls = [];
          for(let i = 0; i < options.length; i++) {
            controls.push(new FormControl());
          }
          this.form.setControl(property.name, new FormArray(controls));
        }
      });
  }

  protected setSelectionOptions(property: Property, options: SelectionOption[]) {
    property.selectionProvider.options = options;
    this.clearDependentSelectionValues(property);
    const selected = options.find(o => o.s);
    if (selected) {
      this.form.get(property.name).setValue(selected);
    }
  }

  protected clearDependentSelectionValues(property: Property) {
    const nextProperty = property.selectionProvider.nextProperty;
    if (nextProperty) {
      this.clearSelectionValues(this.searchProperties.find(p => p.name == nextProperty));
    }
  }

  protected clearSelectionValues(property: Property) {
    this.form.get(property.name).setValue(null);
    property.selectionProvider.options = [];
    const nextProperty = property.selectionProvider.nextProperty;
    if(nextProperty) {
      this.clearSelectionValues(this.searchProperties.find(p => p.name == nextProperty));
    }
  }

  search() {
    this.page = 0;
    this.refreshSearch();
  }

  isDataTable() {
    return this.media.isActive('gt-xs');
  }

  protected loadSearchResultsPage(page: number) {
    this.isLoadingResults = true;
    let params = new HttpParams();
    params = this.composeSearch(params);
    params = params.set("firstResult", (page * this.pageSize).toString());
    params = params.set("maxResults", this.pageSize.toString());
    if(this.sortInfo) {
      params = params.set("sortProperty", this.sortInfo.active);
      params = params.set("sortDirection", this.sortInfo.direction);
    }
    this.http.get<SearchResults>(this.sourceUrl, {params: params}).subscribe(
      results => {
        results.records = results['Result'];
        this.results = results;
        if(this.isDataTable() || !this.resultsDataSource.data) {
          this.resultsDataSource.data = this.results.records;
          this.selection.clear();
        } else {
          this.resultsDataSource.data = [... this.resultsDataSource.data, ... this.results.records];
        }
        this.page = page;
        this.isLoadingResults = false;
      },
      error => {
        this.isLoadingResults = false; //TODO notify error?
      }
    );
  }

  protected composeSearch(params: HttpParams) {
    this.searchProperties.forEach(property => {
      if(property.selectionProvider) {
        params = this.addSelectionProviderSearchParameter(property, params);
      } else {
        params = this.addSimpleSearchParameter(property, params);
      }
    });
    return params;
  }

  protected addSimpleSearchParameter(property, params: HttpParams) {
    const name = property.name;
    const value = this.form.get(name).value;
    if(value == null) {
      return params;
    }
    if (isDateProperty(property)) {
      params = params.set(`search_${name}_min`, value.valueOf().toString());
      params = params.set(`search_${name}_max`, value.valueOf().toString());
    } else if (isNumericProperty(property)) {
      params = params.set(`search_${name}_min`, value.toString());
      params = params.set(`search_${name}_max`, value.toString());
    } else {
      params = params.set(`search_${name}`, value.toString());
    }
    return params;
  }

  protected addSelectionProviderSearchParameter(property, params: HttpParams) {
    const name = property.name;
    let value = this.form.get(name).value;
    if (property.selectionProvider.displayMode == 'CHECKBOX') {
      property.selectionProvider.options.forEach((option, index) => {
        if(value[index]) {
          params = params.append(`search_${name}`, option.v);
        }
      });

    } else {
      if (value != null) {
        value = value.v;
        if (value instanceof Array) {
          value.forEach(v => {
            params = params.append(`search_${name}`, v.toString());
          });
        } else {
          params = params.set(`search_${name}`, value.toString());
        }
      }
    }
    return params;
  }

  handleScrolledIndexChange(index: number) {
    const newPage = Math.round((index + 1) / this.pageSize);
    if(newPage > this.page && !this.isLoadingResults) {
      this.loadSearchResultsPage(newPage);
    }
  }

  loadPage(event: PageEvent) {
    this.loadSearchResultsPage(event.pageIndex);
  }

  sort(sort: Sort) {
    this.sortInfo = sort;
    this.loadSearchResultsPage(0);
  }

  clearSearch() {
    this.form.reset();
    this.searchProperties.forEach(property => {
      const sp = property.selectionProvider;
      if(sp) {
        if(sp.displayMode == 'AUTOCOMPLETE') {
          sp.options = [];
        }
        this.clearDependentSelectionValues(property);
      }
    });
  }

  refreshSearch() {
    //Infinite scrolling appends to the list rather then replace it, so we need to empty it
    this.resultsDataSource.data = [];
    this.loadSearchResultsPage(this.page);
  }

  //Selection
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.resultsDataSource.data.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  toggleSelectAll() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.resultsDataSource.data.forEach(row => this.selection.select(row));
  }

  //Blobs
  getBlobUrl(id: string, propertyName: string) {
    const blobUrl = `${this.sourceUrl}/${id}/:blob/${propertyName}`;
    if(this.portofino.localApiPath) {
      return `${this.portofino.localApiPath}/blobs?path=${encodeURIComponent(blobUrl)}` +
        `&token=${encodeURIComponent(this.auth.jsonWebToken)}`;
    } else {
      return blobUrl;
    }
  }

  getButtons(list = 'default') {
    return getButtons(this, list);
  }

}

export class SearchResults {
  recordsReturned: number;
  totalRecords: number;
  startIndex: number;
  records: object[];
}

export class SearchResultField {
  name: string;
  label: string;
  key = false;
  href: (_: { __rowKey: string } | any) => string;
  routerLink: (_: { __rowKey: string } | any) => string | any[];
  value: (row, field: SearchResultField) => Observable<string> = SearchResultField.defaultValueFn;
  component: Type<any>;
  inList = false;

  getValue(row) {
    return this.value(row, this);
  }

  getRouterLink(row) {
    if(this.routerLink) {
      return this.routerLink(row);
    } else {
      return null;
    }
  }

  getHref(row) {
    if(this.href) {
      return this.href(row);
    } else {
      return null;
    }
  }

  static defaultValueFn(row, field: SearchResultField) {
    return of(row[field.name].displayValue || row[field.name].value);
  }
}
