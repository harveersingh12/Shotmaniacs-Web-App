let memberList = []
let eventlist = [];
let imageList = [];
let completedhours = 0;
let upcominghours = 0;

window.onload = function () {
    fetchAllCrewMembers();
}

/**
 * Removes the session token.
 */
function logOut() {
    sessionStorage.removeItem('token');
}

/**
 * Fetches all the crew members from the database.
 */
function fetchAllCrewMembers(){
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json'
    };

    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    fetch('/shotmaniacs_war/api/crewmembers', {headers: headers})
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
})
        .then(members => {
            memberList = members;
            // fetchImages();
            populateCrewList(members);
        })
        .catch(error => {
            console.error('Error fetching events:', error);
        });
}

/**
 *Populates the crew list.
 * @param members
 */
function populateCrewList(members) {
    const tableBody = document.getElementById('crewlist');

    document.getElementById('not-found').style.display = members.length === 0 ? 'block' : 'none';
    members.forEach((member) => {
        const row = document.createElement('div');
        row.classList.add('list-item');
        row.onclick = () => {
            document.getElementById('crew-info').style.display = 'flex';
            showInfo(member.memberCmid);
            getAllCrewBookings(member.memberCmid)
        };
        const imageSrc = member.memberImage ? 'data:image/jpeg;base64,' + member.memberImage.data : 'static/avatar.png';


        row.innerHTML = `
            <div class="pfp">
                <img src="${imageSrc}" alt="">
            </div>
            <div class="infoandarrow">
                <div class="info">
                    <div class="name">
                        <p>${member.memberName}</p>
                    </div>
                    <div class="job">
                        <p><i>${member.memberJob}</i></p>
                    </div>
                </div>
                <div class="arrow">
                    <h1>></h1>
                </div>
            </div>
        `;

        tableBody.appendChild(row);
    });
}

/**
 * Fetches all bookings for a specific crew member based on the given cmid.
 * @param cmid crew ID
 */
function getAllCrewBookings(cmid) {
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json'
    };

    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    fetch(`/shotmaniacs_war/api/crewmembers/${cmid}/events`, { headers: headers })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(events => {
            eventlist = events;
            upcominghours = 0;
            completedhours = 0;
            document.getElementById('upcoming-total-hours').innerHTML = '0 hour(s)';
            document.getElementById('completed-total-hours').innerHTML = '0 hour(s)';
            displayBookings(events);
        })
        .catch(error => {
            console.error('Error fetching bookings:', error);
        });
}
function displayBookings(events) {
    let upcoming = document.getElementById('upcoming-events-list');
    let completed = document.getElementById('completed-events-list');
    upcoming.innerHTML = '';
    completed.innerHTML = '';
    upcominghours = 0;
    completedhours = 0;
    let eventContainer;
    events.forEach((event) => {

        if(event.status === "ONGOING") {
            upcominghours += event.duration;
            eventContainer = upcoming;
        } else if(event.status === "COMPLETED") {
            completedhours += event.duration;
            eventContainer = completed;
        }

        const row = document.createElement('div');
        row.classList.add('event');
        row.setAttribute('id', `event-${event.eid}`);

        let event_date = event.date.split('T')[0].split("-").reverse().join("-");
        let event_time = event.date.split('T')[1];

        row.innerHTML = `
            <div class="event-info">
                <div class="event-name">
                    <p>${event.name}</p>
                </div>
                <div class="event-type">
                    <p>${event.type}</p>
                </div>
                <div class="event-date-location">
                    <p>${event_date} ${event_time} - ${event.location}</p>
                </div>
                <div class="event-duration">
                    <p>${event.duration} hour(s)</p>
                </div>
            </div>
        `;

        eventContainer.appendChild(row);


    })

    document.getElementById('upcoming-total-hours').innerHTML = upcominghours + ' hour(s)';
    document.getElementById('completed-total-hours').innerHTML = completedhours + ' hour(s)';
}

function filterEvents() {
    let startDate = document.getElementById('start-date').value;
    let endDate = document.getElementById('end-date').value;
    let startTime = document.getElementById('start-time').value;
    let endTime = document.getElementById('end-time').value;

    if (!startDate || !endDate) {
        showPopup('Please enter a start and end date', 'error')
        return;
    }
    if(!startTime) {
        startTime = "00:00:00";
    }
    if(!endTime) {
        endTime = "23:59:59";
    }

    const startDateTime = new Date(`${startDate}T${startTime}`);
    const endDateTime = new Date(`${endDate}T${endTime}`);

    const filteredEvents = eventlist.filter(event => {
        const eventDate = new Date(event.date);
        return eventDate >= startDateTime && eventDate <= endDateTime;
    });

    if (filteredEvents.length > 0){
        const cmid = filteredEvents[0].cmid;
        document.getElementById('upcoming-total-hours').innerHTML = '0 hour(s)';
        document.getElementById('completed-total-hours').innerHTML = '0 hour(s)';
        displayBookings(filteredEvents, cmid);
    } else {
        document.getElementById('event-list').style.visibility = 'hidden';
        document.getElementById('no-events-found').style.visibility = 'visible';
    }
}

function clearCrewFilters() {
    document.getElementById('start-date').value = '';
    document.getElementById('end-date').value = '';
    document.getElementById('start-time').value = '';
    document.getElementById('end-time').value = '';
    document.getElementById('event-list').style.visibility = 'visible';
    document.getElementById('no-events-found').style.visibility = 'hidden';
    displayBookings(eventlist);
}

function showInfo(cmid) {
    const memberInfoContainer = document.getElementById('big-info');

    if (!memberInfoContainer) {
        console.error('Member info container not found');
        return;
    }

    let member = memberList.find(member => member.memberCmid === cmid);

    const imageSrc = member.memberImage ? 'data:image/jpeg;base64,' + member.memberImage.data : 'static/avatar.png';

    memberInfoContainer.innerHTML = `
            <div class="big-pfp">
                <img src="${imageSrc}" alt="" id="avatar">
            </div>
            <div class="heading">
                <div class="name tooltip">
                    <span class="tooltiptext">${member.memberEmail}</span>
                    <p><b>${member.memberName}</b></p>
                </div>
            </div>
            <div class="job">
                <p><b>Role</b></p>
                <p id="job-text">${member.memberRole}</p>
                <p><b>Job</b></p>
                <p id="job-text">${member.memberJob}</p>
            </div>
            <div class="edit-info">
                <button onclick="editCrewInfo(${cmid})">Edit Info</button>
            </div>
    `;
}

function editCrewInfo(crewId) {
    const member = memberList.find(member => member.memberCmid === crewId);

    const memberInfoContainer = document.getElementById('big-info');

    if (!memberInfoContainer) {
        console.error('Member info container not found');
        return;
    }

    memberInfoContainer.innerHTML = `
            <div class="heading">
            <h5>Edit Image</h5>
            <img id="image-preview" src="" alt="Image Preview" style="display:none; max-width: 150px; max-height: 150px;" />
            <input type="file" id="image-input" name="image" accept="image/*" required>
                <div class="name-input">
                    <p><b>Name</b></p>
                    <input type="text" value="${member.memberName}" required>
                </div>
                <div class="email">
                    <p><b>Email</b></p>
                    <input type="text" value="${member.memberEmail}" required>
                </div>
            </div>
            <div class="job" id="edit-job">
                <p><b>Role</b></p>
                <select name="input-role" id="input-role" required>
                    <option value="admin" ${member.memberRole === 'admin' ? 'selected' : ''}>Admin</option>
                    <option value="crewmember" ${member.memberRole === 'crewmember' ? 'selected' : ''}>Crew Member</option>
                </select>
                <p><b>Job</b></p>
                <select name="input-job" id="input-job" required>
                    <option value="photography" ${member.memberJob === 'photography' ? 'selected' : ''}>Photography</option>
                    <option value="film" ${member.memberJob === 'film' ? 'selected' : ''}>Film</option>
                    <option value="marketing" ${member.memberJob === 'marketing' ? 'selected' : ''}>Marketing</option>
                    <option value="productionmanager" ${member.memberJob === 'productionmanager' ? 'selected' : ''}>Production Manager</option>
                    <option value="other" ${member.memberJob === 'other' ? 'selected' : ''}>Other</option>
                </select>
            </div>
            <div class="edit-info">
                <button onclick="saveCrewInfo(${crewId})">Save Info</button>
            </div>
    `;
    const imageInput = memberInfoContainer.querySelector('#image-input');
    const imagePreview = memberInfoContainer.querySelector('#image-preview');

    imageInput.addEventListener('change', function() {
        const file = imageInput.files[0];

        if (file) {
            const reader = new FileReader();

            reader.onload = function(e) {
                imagePreview.src = e.target.result;
                imagePreview.style.display = 'block';
            };

            reader.readAsDataURL(file);
        } else {
            imagePreview.src = '';
            imagePreview.style.display = 'none';
        }
    });

}

function saveCrewInfo(crewId) {
    const memberInfoContainer = document.getElementById('big-info');
    const name = memberInfoContainer.querySelector('.name-input input').value;
    const email = memberInfoContainer.querySelector('.email input').value;
    const role = memberInfoContainer.querySelector('#input-role').value;
    const job = memberInfoContainer.querySelector('#input-job').value;


    const member = memberList.find(member => member.memberCmid === crewId);
    member.memberName = name;
    member.memberEmail = email;
    member.memberRole = role;
    member.memberJob = job;

    const imageInput = memberInfoContainer.querySelector('#image-input');
    if (imageInput.files.length > 0) {
        const file = imageInput.files[0];
        const reader = new FileReader();

        reader.onloadend = function() {
            member.memberImage = reader.result.split(',')[1];

            updateMember(member);
        };

        reader.readAsDataURL(file);
    } else {
        updateMember(member);
    }

    showInfo(member.memberCmid);
}

function updateMember(member) {
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json'
    };

    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    fetch(`/shotmaniacs_war/api/crewmembers/${member.memberCmid}`, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify(member)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .catch(error => {
            console.error('Error updating member:', error);
        });
}



