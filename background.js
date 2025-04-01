var config = {
  mode: "fixed_servers",
  rules: {
    singleProxy: {
      scheme: "http",
      host: "gate.nodemaven.com",
      port: 8080
    },
    bypassList: ["localhost", "127.0.0.1"]
  }
};

chrome.proxy.settings.set({ value: config, scope: "regular" }, function() {});

chrome.webRequest.onAuthRequired.addListener(
  function(details) {
    return {
      authCredentials: {
        username: "andrew_keensightanalytics_com-country-any-sid-7a3253070d104-filter-medium",
        password: "s8e6jgcgue"
      }
    };
  },
  { urls: ["<all_urls>"] },
  ["blocking"]
);

