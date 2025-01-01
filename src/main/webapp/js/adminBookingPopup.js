let allCrewMembers;
let currentSelectedEvent;
let selectedCmids;
let selectedJobs;
let allEvents = []


//Event listener for the DOMContentLoaded event.
//Fetch all crew members when the DOM is fully loaded.
document.addEventListener('DOMContentLoaded', (event) => {
    fetchALLCrew();
});

/**
 * Set up the request headers, including the token (if it exists)
 * and adds event listeners to the crew search input for the search functionality.
 */
window.onload = function () {
    selectedCmids = []
    selectedJobs = []
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
            allCrewMembers = members;
        })
        .catch(error => {
            console.error('Error fetching events:', error);
        });

    fetchAllEvents();

    const crewSearch = document.getElementById('crewSearch');
    crewSearch.addEventListener('input', searchCrewMembers);
    crewSearch.addEventListener('click', searchCrewMembers);
};

/**
 *Filter the crew members based on the search query and
 * @param event the input from the user on the search field
 */
function searchCrewMembers(event) {
    const searchQuery = event.target.value.toLowerCase();
    const searchResults = document.getElementById('searchResults');
    //clear any previous search results
    searchResults.innerHTML = '';

    if (allCrewMembers) {
        // Filter the crew members based on the search query
        const filteredCrewMembers = allCrewMembers.filter(member =>
            member.memberName.toLowerCase().includes(searchQuery));

        // Loop through each filtered crew member and create a list
        filteredCrewMembers.forEach(member => {
            const listItem = document.createElement('li');
            listItem.textContent =
                `${member.memberName} (${member.memberRole}) (${member.memberJob})`;

            // Add a click event listener to each list item
            listItem.addEventListener('click', () => {
                //check if the max number of allowed members has been reached
                if (currentSelectedEvent.currentmembers === currentSelectedEvent.maxmembers) {
                    showPopup("You cannot add more than " + currentSelectedEvent.maxmembers
                        + " members", "error")
                    return;
                }
                //check if the booking already has one production manager
                if (member.memberJob === "productionmanager" &&
                    selectedJobs.includes(member.memberJob)) {
                    showPopup("There is another production manager in this booking!",
                        "error")
                    return;
                }
                //check if crew member is already in the booking
                if (selectedCmids.includes(member.memberCmid)) {
                    showPopup("This member is already in the booking!", "error")
                    return;
                }

                listItem.remove();
                selectedCmids.push(member.memberCmid);
                selectedJobs.push(member.memberJob);

                currentSelectedEvent.currentmembers += 1;

                // add it to the current crewmember list
                const crewMemberList = document.getElementById('crewMemberList');
                const crewMemberListItem = document.createElement('li');
                crewMemberListItem.textContent =
                    `${member.memberName} (${member.memberRole}) (${member.memberJob})`;

                // Add a click event listener to each list item in the current crew members list
                crewMemberListItem.addEventListener('click', () => {
                    currentSelectedEvent.currentmembers -= 1;
                    crewMemberListItem.remove();

                    // Remove the member from the selected members and jobs arrays
                    let indexToRemove = selectedCmids.indexOf(member.memberCmid);
                    if (indexToRemove !== -1) {
                        selectedCmids.splice(indexToRemove, 1);
                    }

                    indexToRemove = selectedJobs.indexOf(member.memberJob);
                    if (indexToRemove !== -1) {
                        selectedJobs.splice(indexToRemove, 1);
                    }
                });

                crewMemberList.appendChild(crewMemberListItem);
            });
            searchResults.appendChild(listItem);
        });
    }
}

/**
 * Fetches all events from the server and populate the events table.
 */
function fetchAllEvents() {
    //retrieve session token for API authorization
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json'
    };
    //if token exists, add the Authorization header
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    fetch('/shotmaniacs_war/api/event', {headers: headers})
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(events => {
            console.log('Fetched events:', events);
            allEvents = events;
            populateTable(events);
        })
        .catch(error => {
            console.error('Error fetching events:', error);
        });
}

/**
 * Fetches all crew members from the server.
 */
function fetchALLCrew() {
    //retrieve session token for API authorization
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json'
    };
    //if token exists, add the Authorization header
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
        .then(crew => {
            allCrew = crew;
        })
        .catch(error => {
            console.error('Error fetching crew:', error);
        });

}

/**
 * Populates the event table with the given events.
 * Adds a new row for each event.
 * @param events array of events that will populate the table
 */
function populateTable(events) {
    const tableBody = document.querySelector('.styled-table tbody');
    //clear any previous table content
    tableBody.innerHTML = '';
    //iterate through each event and create a table row for it
    events.forEach((event, index) => {
        const row = document.createElement('tr');
        row.classList.add('booking');
        //add the on click functionality to the row
        row.onclick = () => showBookingInfo(event);
        //populate the row with the event information
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${event.name}</td>
            <td>${event.date}</td>
            <td>${event.type}</td>
            <td>${event.isaccepted}</td>
        `;

        tableBody.appendChild(row);
    });
}

/**
 * Display information about the selected event.
 * @param event the selected event to be viewed
 */
function showBookingInfo(event) {
    const crewMemberList = document.getElementById('crewMemberList');

    // Clear any previous content
    crewMemberList.innerHTML = '';

    selectedCmids = []
    selectedJobs = []
    currentSelectedEvent = event;

    const currentCrewmembers = document.getElementById('currentCrewmembers');
    currentCrewmembers.textContent = "Current Crewmembers";

    //retrieve session token for API authorization
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json'
    };
    //if token exists, add the Authorization header
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    fetch(`/shotmaniacs_war/api/contract/${currentSelectedEvent.eid}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            return response.json();
        })
        .then(result => {
            console.log("Current event contracts: " + result)
            //take each fetched member individually
            result.forEach(member => {
                selectedCmids.push(member.cmid);
                selectedJobs.push(member.job)

                const crewMemberList =
                    document.getElementById('crewMemberList');

                const crewMemberListItem = document.createElement('li');
                crewMemberListItem.textContent = `${member.name} (${member.role}) (${member.job})`;

                //add click event listener to remove crew member from list
                crewMemberListItem.addEventListener('click', () => {
                    currentSelectedEvent.currentmembers -= 1;
                    crewMemberListItem.remove();

                    //remove the member's id and job from the arrays
                    let indexToRemove = selectedCmids.indexOf(member.cmid);
                    if (indexToRemove !== -1) {
                        selectedCmids.splice(indexToRemove, 1);
                    }

                    indexToRemove = selectedJobs.indexOf(member.job);
                    if (indexToRemove !== -1) {
                        selectedJobs.splice(indexToRemove, 1);
                    }
                });

                crewMemberList.appendChild(crewMemberListItem);
                console.log(selectedCmids);
            });

            const modal = document.getElementById('eventModal');
            const eventDetails = document.getElementById('eventDetails');

            //clear any previous content
            eventDetails.innerHTML = '';

            //create form elements dynamically
            const form = document.createElement('form');
            form.setAttribute('id', 'editForm');

            //what to not edit
            const strings = ["clientName", "clientEmail", "cid",
                "currentmembers", "eid", "productionmanager"]

            //go through the event properties
            const keys = Object.keys(event);
            keys.filter(key => !strings.includes(key)).forEach(key => {
                const detailRow = document.createElement('div');
                detailRow.classList.add('event-detail');

                //create editable fields based on value type
                let inputField;
                if (key === "status") {
                    inputField = document.createElement('select');
                    inputField.setAttribute('name', key);
                    //status field options
                    let optionOngoing = document.createElement('option');
                    optionOngoing.setAttribute('value', 'ONGOING');
                    optionOngoing.textContent = 'Ongoing';

                    let optionCompleted =
                        document.createElement('option');

                    optionCompleted.setAttribute('value', 'COMPLETED');
                    optionCompleted.textContent = 'Completed';

                    inputField.appendChild(optionOngoing);
                    inputField.appendChild(optionCompleted);

                    //check if the event is ongoing or completed
                    if (event[key] === "ONGOING") {
                        optionOngoing.setAttribute('selected', true);
                    } else {
                        optionCompleted.setAttribute('selected', true);
                    }
                } else if (key === "date") {
                    inputField = document.createElement('input');
                    inputField.setAttribute('type', 'datetime-local');
                    inputField.setAttribute('name', key);
                    inputField.setAttribute('value', event[key]);

                } else if (key === "bookingtype") {
                    inputField = document.createElement('select');
                    inputField.setAttribute('name', key);

                    //options for booking type
                    let optionNotset = document.createElement('option');
                    optionNotset.setAttribute('value', 'NOTSET');
                    optionNotset.textContent = 'Not Set';

                    let optionPhotography =
                        document.createElement('option');

                    optionPhotography.setAttribute('value', 'PHOTOGRAPHY');
                    optionPhotography.textContent = 'Photography';

                    let optionFilm = document.createElement('option');
                    optionFilm.setAttribute('value', 'FILM');
                    optionFilm.textContent = 'Film';

                    let optionMarketing =
                        document.createElement('option');

                    optionMarketing.setAttribute('value', 'MARKETING');
                    optionMarketing.textContent = 'Marketing';

                    inputField.appendChild(optionNotset);
                    inputField.appendChild(optionPhotography);
                    inputField.appendChild(optionFilm);
                    inputField.appendChild(optionMarketing);

                    if (event[key] === "NOTSET") {
                        optionNotset.setAttribute('selected', true);
                    } else if (event[key] === "PHOTOGRAPHY") {
                        optionPhotography.setAttribute('selected', true);
                    } else if (event[key] === "FILM") {
                        optionFilm.setAttribute('selected', true);
                    } else if (event[key] === "MARKETING") {
                        optionMarketing.setAttribute('selected', true);
                    }

                } else if (typeof event[key] === 'string' || typeof event[key] === 'number') {
                    //create input field for string or number properties
                    inputField = document.createElement('input');
                    inputField.setAttribute('type', 'text');
                    inputField.setAttribute('name', key);
                    inputField.setAttribute('value', event[key]);
                } else if (typeof event[key] === 'boolean') {
                    //create checkbox
                    inputField = document.createElement('input');
                    inputField.setAttribute('type', 'checkbox');
                    inputField.setAttribute('name', key);
                    //convert to boolean
                    inputField.checked = !!event[key];
                } else {
                    inputField = document.createElement('span');
                    inputField.textContent = event[key];
                }

                //label for the field
                const label = document.createElement('label');
                if (key === "isaccepted") {
                    label.textContent = "Booking accepted: ";
                } else if (key === "maxmembers") {
                    label.textContent = "Max crew members allowed: ";
                } else {
                    label.textContent = `${key}: `;
                }
                label.setAttribute('for', key);

                detailRow.appendChild(label);
                detailRow.appendChild(inputField);


                form.appendChild(detailRow);
            });

            //create save button
            const saveButton = document.createElement('button');
            saveButton.textContent = 'Save';
            saveButton.setAttribute('type', 'submit');
            form.appendChild(saveButton);

            const declineButton = document.createElement('button');
            declineButton.textContent = 'Delete';
            declineButton.onclick = declineBooking;
            declineButton.setAttribute('type', 'button');
            form.appendChild(declineButton);

            eventDetails.appendChild(form);

            form.addEventListener('submit', saveEvent);

            //display modal
            modal.style.display = 'block';

            //close modal if the user clicks outside of it
            window.onclick = (event) => {
                if (event.target === modal) {
                    modal.style.display = 'none';
                }
            };
        })

    /**
     *Handles the process of updating event details and assigning crew members to an event.
     * @param event the event submitted through the form
     */
    const saveEvent = (event) => {
        event.preventDefault();

        const formData = new FormData(event.target);

        //construct object from FormData
        const formDataObj = {};
        formData.forEach((value, key) => {
            // Attempt to convert values to appropriate types
            if (!isNaN(value)) { // Check if value is a number
                formDataObj[key] = parseInt(value);
            } else if (event.target.elements[key].type === 'checkbox') {
                formDataObj[key] = event.target.elements[key].checked;
            } else {
                formDataObj[key] = value;
            }
        });
        //ensure isaccepted is explicitly set to false
        if (!formDataObj.isaccepted) {
            formDataObj.isaccepted = false;
        }

        const eid = currentSelectedEvent.eid;
        formDataObj.currentmembers = currentSelectedEvent.currentmembers

        const token = sessionStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        fetch(`/shotmaniacs_war/api/event/${eid}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formDataObj)
        })
            .then(response => {
                if (!response.ok) {
                    console.log("Failed to update event!")
                    showPopup("Failed to update event. Check spelling.", "error")
                }
                else{

                    //assign crew members to the event
                    const eid = currentSelectedEvent.eid;

                    //before reassigning the contract first remove the existing contracts
                    //for this eid
                    const token = sessionStorage.getItem('token');
                    const headers = {
                        'Content-Type': 'application/json'
                    };
                    if (token) {
                        headers['Authorization'] = 'Bearer ' + token;
                    }
                    fetch(`/shotmaniacs_war/api/contract/${eid}`, {
                        method: 'DELETE',
                        headers: headers
                    })
                        .then(response => {
                            selectedCmids.forEach((value, index) => {
                                const token = sessionStorage.getItem('token');
                                const headers = {
                                    'Content-Type': 'application/json'
                                };

                                if (token) {
                                    headers['Authorization'] = 'Bearer ' + token;
                                }
                                fetch('/shotmaniacs_war/api/contract', {
                                    method: 'POST',
                                    headers: headers,
                                    body: JSON.stringify({ "eid": eid, "cmid": value })
                                })
                            });

                            const modal =
                                document.getElementById('eventModal');

                            modal.style.display = 'none';

                            fetchAllEvents();
                        });
                }
            })
            .catch(error => {
                console.error('Error updating event:', error);
            });
    }

    /**
     * Deletes the selected event by sending a DELETE request to the server.
     */
    const declineBooking = () => {
        const token = sessionStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        fetch(`/shotmaniacs_war/api/event/${currentSelectedEvent.eid}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (!response.ok) {
                    showPopup("Failed to decline event.", "error")
                }
                else{
                    fetchAllEvents();
                    const modal = document.getElementById('eventModal');
                    modal.style.display = 'none';
                }
            })
            .catch(error => {
                console.error('Error deleting the event:', error);
            });
    }
};