JhipsterEmber.LoginController = Ember.ObjectController.extend(Ember.SimpleAuth.LoginControllerMixin, {
    identification: "",
    password: "",
    authenticator: "app:authenticators:custom"
});
