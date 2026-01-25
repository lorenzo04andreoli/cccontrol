(function () {
  function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
  }

  function getMeta(name) {
    return document.querySelector(`meta[name="${name}"]`)?.getAttribute('content');
  }

  window.csrfHeader = function csrfHeader() {
    const metaToken = getMeta('_csrf');
    const metaHeader = getMeta('_csrf_header') || 'X-CSRF-TOKEN';

    const cookieToken = getCookie('XSRF-TOKEN');
    const token = metaToken || cookieToken;

    if (!token) return {};

    const t = decodeURIComponent(token);

    return {
      [metaHeader]: t,
      'X-XSRF-TOKEN': t
    };
  };

  window.apiFetch = function apiFetch(url, options = {}) {
    const headers = {
      ...(options.headers || {}),
      ...csrfHeader()
    };

    return fetch(url, {
      credentials: 'same-origin',
      ...options,
      headers
    });
  };
})();
