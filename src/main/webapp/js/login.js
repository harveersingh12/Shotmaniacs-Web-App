document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const data = {
        name: formData.get('name'),
        password: formData.get('password'),
    };

    fetch('/shotmaniacs_war/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(responseData => {
            sessionStorage.setItem('token', responseData.token);

            const role = responseData.role;

            if(responseData.status === "LOGIN_INVALID"){
                showPopup("Username or password incorrect", "error");
                return;
            }

            if(role === "crewmember")
                window.location.href = '/shotmaniacs_war/crew-dashboard.html';
            else if (role === "admin")
                window.location.href = '/shotmaniacs_war/admin-dashboard.html';
        })
        .catch(error => {
            console.error('Error:', error);
        });
});

function showPassword() {
    var x = document.getElementById("password");
    if (x.type === "password") {
        x.type = "text";
    } else {
        x.type = "password";
    }
}