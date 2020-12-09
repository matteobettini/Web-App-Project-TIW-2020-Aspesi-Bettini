/**
 * Login management
 */
(function() {
    //Link graphics
    var login_button = document.getElementById("login_button");
    var register_button = document.getElementById("register_button");
    var open_register_button = document.getElementById("open_register_button");
    var login_warning_div = document.getElementById('login_warning_id');
    var register_div = document.getElementById("register-div");
    var password_input = register_button.closest("form").querySelector('input[name="password"]');
    var repeat_password_input = register_button.closest("form").querySelector('input[name="passwordConfirm"]');
    var register_warning_div = document.getElementById('register_warning_id');

    //Attach to login button
    login_button.addEventListener("click", (e) => {
        var form = e.target.closest("form"); 
        login_warning_div.style.display = 'none';
        if (form.checkValidity()) { //Do form check
            sendToServer(form, login_warning_div, 'Login');
        }else 
            form.reportValidity(); //If not valid, notify
    });

    //Attach to register button
    register_button.addEventListener("click", (e) => {
        var form = e.target.closest("form"); 
        register_warning_div.style.display = 'none';
        if (form.checkValidity()) { //Do form check
            //Do additional checks
            if (repeat_password_input.value != password_input.value){
                register_warning_div.textContent = "Passwords do not match";
                register_warning_div.style.display = 'block';
                return;
            }
            sendToServer(form, register_warning_div, 'Register');
        }else 
            form.reportValidity(); //If not valid, notify
    });

    //Attach to register view button
    open_register_button.addEventListener("click", function(e){
        if(e.target.textContent === "Register now!"){
            e.target.textContent = "Hide register form";
            register_div.style.display = 'block';
        }else{
            e.target.textContent = "Register now!";
            register_div.style.display = 'none';
        }
    });

    function sendToServer(form, error_div, request_url){
        makeCall("POST", request_url, form, function(req){
            switch(req.status){ //Get status code
                case 200: //Okay
                    var data = JSON.parse(req.responseText);
                    sessionStorage.setItem('id', data.id);
                    sessionStorage.setItem('name', data.name);
                    window.location.href = "home.html";
                    break;
                case 400: // bad request
                case 401: // unauthorized
                case 500: // server error
                    error_div.textContent = req.responseText;
                    error_div.style.display = 'block';
                    break;
                default: //Error
                    error_div.textContent = "Request reported status " + req.status;
                    error_div.style.display = 'block';
            }
        });
    }
})();

