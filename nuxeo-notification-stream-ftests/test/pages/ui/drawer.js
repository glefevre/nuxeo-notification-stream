import BasePage from '../base';

export default class Drawer extends BasePage {
  get menu() {
    return this.el.element('#menu');
  }

  get pages() {
    return this.el.element('iron-pages');
  }

  get logo() {
    return this.el.element('#logo');
  }

  get browser() {
    return this._section('browser');
  }

  get search() {
    return this._section('search');
  }

  get administration() {
    return this._section('administration');
  }

  get personal() {
    return this._section('personalWorkspace');
  }

  get profile() {
    return this._section('profile');
  }

  open(name) {
    this.menu.waitForVisible();
    this.menu.waitForVisible(`nuxeo-menu-icon[name='${name}']`);
    this.menu.click(`nuxeo-menu-icon[name='${name}']`);
    const section = this._section(name);
    section.waitForVisible();
    return section;
  }

  _section(name) {
    return this.pages.element(`[name='${name}']`);
  }

  _search(name) {
    return this.pages.element(`[search-name='${name}']`);
  }
}
