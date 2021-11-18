window.onload = function () {
  var inputs = document.getElementsByClassName('send-button');
  for (var i = 0; i < inputs.length; i++) {
    inputs[i].addEventListener('click', async id => {
      try {
        const response = await fetch('command', {
          method: 'post',
          headers: {
            'Content-Type': 'application/json',
          },
          body: '{\"id\": '.concat(id.path[0].name).concat('}'),
        });
        id.path[0].className = "btn btn-success float-right";
        console.log('Completed!', response);
      } catch (err) {
        console.error(`Error: ${err}`);
      }
    });
  }

  var sendAllButtons = document.getElementsByClassName('send-all-button');
  console.log(sendAllButtons.length)
  for (var i = 0; i < sendAllButtons.length; i++) {
    sendAllButtons[i].addEventListener("click", function () {
      var buttons = this.parentNode.getElementsByClassName('send-button');
      for (let index = 0; index < buttons.length; index++) {
        buttons[index].click();
      }
    });
  }
}