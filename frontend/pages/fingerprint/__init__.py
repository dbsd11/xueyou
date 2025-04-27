JS = """async function() {
        const fpPromise = import('https://openfpcdn.io/fingerprintjs/v4').then(FingerprintJS => FingerprintJS.load())
      // Get the visitor identifier when you need it.
      fpPromise
        .then(fp => fp.get())
        .then(result => {
          // This is the visitor identifier:
          localStorage.setItem('visitorId', result.visitorId)
          console.log('Get fingerprint success!')
        })
}"""