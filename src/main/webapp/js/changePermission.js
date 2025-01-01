let allCrewMembersForPermissions;
/**
 * Displays the change permissions pop-up.
 * Populates the table with crew member details and allows job updates.
 */
function changePermissions(){
    const changePermissions = document.getElementById('change-permissions');
    changePermissions.innerHTML = ``;
    changePermissions.innerHTML = `
<style>
#change-permissions-table {
    border: 1px solid var(--shotmaniacs-blue);
    border-collapse: collapse;
    border-radius: 10px;
    overflow: hidden;
    margin: 25px 0;
    font-size: 1em;
    font-family: 'Arial', sans-serif;
    min-width: 1000px;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1); 
}

#change-permissions-table thead tr {
    background-color: var(--shotmaniacs-blue);
    color: white;
    text-align: left;
}

#change-permissions-table th,
#change-permissions-table td {
    padding: 12px 20px;
    font-size: 16px;
    border-bottom: 1px solid #dddddd;
}

#change-permissions-table tbody tr {
    border-bottom: 1px solid #dddddd;
    transition: background-color 0.3s ease;
}

#change-permissions-table tbody tr:nth-of-type(even) {
    background-color: rgba(22,150,210,0.11);
}

#change-permissions-table tbody tr:hover {
    background-color: #f1f1f1;
}

#change-permissions-table tbody tr.active-row {
    font-weight: bold;
    color: var(--shotmaniacs-blue);
}

#change-permissions-table thead th {
    border-bottom: 2px solid #dddddd;
}

.inline-form {
    display: flex;
    align-items: center;
}

.inline-form select {
    margin-right: 10px;
    padding: 5px 10px;
    font-size: 16px;
}

.inline-form button {
    background-color: var(--shotmaniacs-blue);
    color: white;
    border: none;
    border-radius: 5px;
    padding: 8px 16px;
    font-size: 16px;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.inline-form button:hover {
    background-color: #0056b3;
}
#change-permissions-popup{
padding: 30px;
border-radius: 15px;
overflow-y: auto;
max-height: 80vh;
}

#permission-title{
color: black;
}

#table-container {
    overflow-y: auto;
    max-height: 400px; /* Set a max-height to enable scrolling */
}

</style>
         <container class="change-permissions-popup" id="change-permissions-popup">
         <h2 id = "permission-title">Change a crew member's job</h2>
            <table id="change-permissions-table">
                <thead>
                    <tr>
<!--                        <th></th>-->
                        <th>Member</th>
                        <th>Current Job</th>
                        <th>Update Job</th>
                    </tr>
                </thead>
                <tbody>
                <!-- needs to stay to be able to populate the table -->
                </tbody>
            </table>
            <button class="custom-button right-side-button" onclick="closeChangePermissions()">Close</button>
            <p style="font-size: 12px;"><br>*The "Production Manager" job can only be assigned to an administrator.*</p>
         </container>
        `;
    changePermissions.style.display = "flex";
    fetchCrewMembers();
}

/**
 * Closes the permission pop-up.
 */
function closeChangePermissions() {
    const newAccPopup = document.getElementById('change-permissions');
    newAccPopup.style.display = 'none';
}

/**
 * Fetches all crew members from the API.
 * Stores the fetched crew members and populates the permissions table.
 */
function fetchCrewMembers(){
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json'
    };

    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    fetch('/shotmaniacs_war/api/crewmembers', { headers: headers })
        .then(response => response.json()
        )
        .then(crewmembers => {
            allCrewMembersForPermissions = crewmembers;
            populateCrewTable(crewmembers);
        })
        .catch(error => {
            console.error('Error fetching crew members:', error);
        });
}

/**
 * Populates the change permissions table with crew member data.
 * @param crewmembers the crew members who need to be added to the table
 */
function populateCrewTable(crewmembers) {
    const tableBody = document.querySelector('#change-permissions-table tbody');
    tableBody.innerHTML = '';

    crewmembers.forEach((crewmember) => {
        const row = document.createElement('tr');
        row.classList.add('member');

        row.innerHTML = `
<style xmlns="http://www.w3.org/1999/html">
.inline-form {
            display: flex;
            align-items: center;
        }

        .inline-form select {
            margin-right: 30px;
        }

        .wrap {
            display: inline-block;
        }
</style>
<!--            <td id="show-admin"></td>-->
            <td>${crewmember.memberName}</td>
            <td>${crewmember.memberJob}</td>
            <td><form class="inline-form" id="change-job-form">
                 <select id="job" name="job" required>
                    <option value="nothing">Select</option>
                    <option value="photographer">Photographer</option>
                    <option value="film">Film</option>
                    <option value="marketing">Marketing</option>
                    <option value="productionmanager">Production Manager</option>
                    <option value="other">Other</option>
                </select>
                <div class="wrap">
                    <button type="submit" onclick="updateJob()" > 
                        Submit
                    </button>
                </div>
                </form>
            </td>
        `;
        // if(crewmember.memberRole == 'admin'){
        //     const showAdmin = document.getElementById('show-admin');
        //     showAdmin.textContent = "A";
        // }
        tableBody.appendChild(row);
    });
}
/**
 * Updates the job role of a crew member based on the job chosen in the form.
 */
function updateJob() {
    let completedForm;
    let completedFormJob;
    let forms = document.querySelectorAll('#change-permissions-table form');
    let formIndex = 0;
    forms.forEach(function(form) {
        let formData = new FormData(form);
        if(formData.get('job') != "nothing"){
            completedForm = formIndex;
            completedFormJob = formData.get('job');
        }
        formIndex++;
    });
    const matchedName = allCrewMembersForPermissions[completedForm].memberName;
    if(completedFormJob == 'productionmanager' && allCrewMembersForPermissions[completedForm].memberRole != 'admin'){
        alert(`${matchedName} is not an admin. Only admins can be Production Managers.`);
    } else {

        const data = {
            name: matchedName,
            job: completedFormJob
        };
        const token = sessionStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json'
        };
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        fetch(`/shotmaniacs_war/api/crewmembers/jobs`, {
            method: 'PUT',
            headers: headers,
            body: JSON.stringify(data)
        })
            .then(response => response.json())
            .then(data => {
                fetchCrewMembers();
            })
            .catch(error => {
                console.error('Error updating job:', error);
            });
    }
}
