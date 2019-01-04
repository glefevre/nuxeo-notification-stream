import {
  Then,
  When,
} from 'cucumber';
import Login from '../../pages/login';
import UI from '../../pages/ui';

When('I login as {string}', function (username) {
  const login = Login.get();
  login.username = username;
  login.password = username;
  login.submit();
  this.username = username;
  this.ui = UI.get();
  driver.waitForVisible('nuxeo-page');
});

When('I logout', () => Login.get());

Then('I am logged in as {string}', function (username) {
  const currentUser = this.ui.drawer.open('profile').getText('.header').toLowerCase();
  currentUser.should.be.equal(username.toLowerCase());
});

Then('I am logged out', () => driver.isVisible('#username').should.be.true);
