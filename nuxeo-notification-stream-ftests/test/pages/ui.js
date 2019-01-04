import Drawer from './ui/drawer';
import BasePage from './base';

export default class UI extends BasePage {
  goHome() {
    this.drawer.logo.click();
  }

  reload() {
    driver.refresh();
  }

  get drawer() {
    return new Drawer('div[slot="drawer"]');
  }

  static get() {
    driver.url(process.env.NUXEO_URL ? '' : 'ui');
    if (!global.locale) {
      browser.waitForVisible('nuxeo-app:not([unresolved])');
      /* global window */
      const locale = browser.execute(() => window.nuxeo.I18n.language || 'en');
      if (locale.value) {
        global.locale = locale.value;
        moment.locale(global.locale);
      }
    }
    return new UI('nuxeo-app');
  }

  get pages() {
    return this.el.element('#pages');
  }

  get isConnectionActive() {
    /* global document */
    return driver.execute(() => document.querySelector('nuxeo-connection').active).value;
  }

  waitRequests() {
    driver.waitUntil(() => !this.isConnectionActive, 5000, 'Waiting for inactive connection');
  }

  view(option) {
    const selection = option.toLowerCase();
    const dropdown = driver.element('paper-toolbar paper-dropdown-menu #menuButton');
    dropdown.click('#trigger');
    dropdown.waitForVisible('#dropdown');
    dropdown.click(`#dropdown #contentWrapper div paper-menu div paper-icon-item[name="${selection}"]`);
  }

  waitForToastNotVisible() {
    driver.waitUntil(() => driver.elements('paper-toast').value.every(toast => !toast.isVisible()));
  }
}
