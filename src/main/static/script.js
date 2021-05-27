window.onload = function() {
    var inputs = document.getElementsByTagName('button');
    for(var i = 0; i < inputs.length; i++) {
        inputs[i].addEventListener('click', async id => {
          try {
            const response = await fetch('command', {
              method: 'post',
              headers: {
                  'Content-Type': 'application/json',
              },
              body: '{\"id\": '.concat(id.path[0].name).concat('}');,
            });
            id.path[0].className = "btn btn-success float-right";
            console.log('Completed!', response);
          } catch(err) {
            console.error(`Error: ${err}`);
          }
        });
    }
}