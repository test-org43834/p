import {Component, Input, OnInit} from '@angular/core';
import {PortofinoComponent} from "../portofino-app.component";
import {PortofinoService} from "../portofino.service";
import {HttpClient} from "@angular/common/http";
import {FormControl} from "@angular/forms";
import {MatSnackBar} from "@angular/material";
import {Operation, Page, PageConfiguration} from "../page";
import {AuthenticationService} from "../security/authentication.service";
import {Router} from "@angular/router";
import {Button} from "../buttons";
import {NotificationService} from "../notifications/notification.service";

@Component({
  selector: 'portofino-many-to-many',
  templateUrl: './many-to-many.component.html',
  styleUrls: ['./many-to-many.component.css']
})
@PortofinoComponent({ name: 'manyToMany' })
export class ManyToManyComponent extends Page implements OnInit {

  @Input()
  configuration: PageConfiguration & any;
  sourceUrl: string;
  saveEnabled: boolean;
  keySelector = new FormControl();
  keys: Key[];
  key: Key;
  associations: Association[];

  readonly associationsPath = "/:availableAssociations";

  constructor(
    protected http: HttpClient, public portofino: PortofinoService, protected router: Router,
    public authenticationService: AuthenticationService, protected notificationService: NotificationService) {
    super(portofino, http, router, authenticationService);
  }

  ngOnInit() {
    this.keySelector.valueChanges.subscribe(value => {
      this.key = value;
      const onePk = value.key;
      this.http.get<any>(this.sourceUrl + this.associationsPath + '/' + onePk).subscribe(assocs => {
        this.associations = [];
        let keys = assocs.schema.properties[onePk].items.enum;
        keys.forEach(k => {
          let key = new Key();
          key.key = k;
          key.label = assocs.form[0].titleMap[k];
          let association = new Association();
          association.key = key;
          association.selected = assocs.model[onePk].includes(k);
          this.associations.push(association);
        });
      });
    });
    this.sourceUrl = this.computeSourceUrl();
    this.http.get<Operation[]>(this.sourceUrl + this.operationsPath).subscribe(ops => {
      this.saveEnabled = this.operationAvailable(ops,"POST");
    });
    this.http.get<Keys>(this.sourceUrl).subscribe(keys => {
      this.keys = keys.keys;
      if(this.keys.length == 1) {
        this.keySelector.setValue(this.keys[0]);
      }
    });
  }

  static saveEnabled(self: ManyToManyComponent) {
    return self.saveEnabled;
  }

  static key(self: ManyToManyComponent) {
    return self.key;
  }

  @Button({
    icon: "save",
    text: "Save",
    color: "primary",
    presentIf: ManyToManyComponent.saveEnabled,
    enabledIf: ManyToManyComponent.key
  })
  save() {
    if(!this.key) {
      return;
    }
    let body = {};
    body[this.key.key] = this.associations.filter(a => a.selected).map(a => a.key.key);
    this.http.post(this.sourceUrl, body).subscribe(_ => {
      this.notificationService.info('Saved');
    }, error => {
      this.notificationService.error('Error');
    });
  }
}

export class Keys {
  keys: Key[];
}

export class Key {
  key: string;
  label: string;
}

export class Association {
  key: Key;
  selected: boolean;
}