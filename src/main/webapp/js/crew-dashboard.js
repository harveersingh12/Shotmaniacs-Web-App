let allEvents;  //global variable to store the events, mainly used for filter
let eventMonthsAndYears = [];
const monthInDateDigit = 5;
const yearInDateDigit = 0;
const dayInDateDigit = 8;
const timeInDateDigit = 11;
let currentSelectedEvent;
let allAnnouncements = [];

window.onload = function() {
    fetchAllEvents();
    fetchAllAnnouncements();
};

/**
 * Fetches all announcements from the API.
 */
function fetchAllAnnouncements() {
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json'
    };

    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    fetch('/shotmaniacs_war/api/announcement', { headers: headers})
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(announcements => {
            allAnnouncements = announcements;
            populateAnnouncements(announcements);
        })
        .catch(error => {
            console.error('Error fetching announcements:', error);
        });
}

/**
 * Populates the announcement UI from the dashboard.
 * @param announcements announcements to be added to the dashboard
 */
function populateAnnouncements(announcements) {
    const announcementBox = document.querySelector('.announcement-table tbody');
    announcementBox.innerHTML = '';

    announcements.forEach((announcement, index) => {
        const announcementItem = document.createElement('tr');
        announcementItem.classList.add('announcement-item');
        let announcementurgencystring = '';
        switch (announcement.urgency){
            case 0:
                announcementurgencystring = "Low";
                break;
            case 1:
                announcementurgencystring = "Medium";
                break;
            case 2:
                announcementurgencystring = "High";
                break;
            default:
                announcementurgencystring = "Error";
                break;

        }

        announcementItem.innerHTML = `
            <td>
                <h4>${announcement.announcementName}:</h4>
                <p>${announcementurgencystring}</p>
                <p>${announcement.announcementDate}</p>
                <p>${announcement.announcementMessage}</p>
            </td>
        `;

        announcementBox.appendChild(announcementItem);
    });
}

/**
 * Fetches all events from the API.
 */
function fetchAllEvents() {
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json'
    };
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    fetch('/shotmaniacs_war/api/event/crew', { headers: headers})
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(events => {
            allEvents = events;
            populateTable(events);
        })
        .catch(error => {
            console.error('Error fetching events:', error);
        });
}

/**
 * Populates the dashboard with events.
 * @param events
 */
function populateTable(events) {
    const showBookingsDiv = document.querySelector('#show-bookings-div');
    showBookingsDiv.innerHTML = '';

    events.forEach((event, index) => {
            const monthDividerDiv = document.createElement('div');
            const oneEventDiv = document.createElement('div');
            oneEventDiv.className = 'event-div';
            if(event.status == "COMPLETED"){
                oneEventDiv.classList.add('completed');
             }
            oneEventDiv.onclick = () => {
                    if (oneEventDiv.classList.contains('expanded')) {
                        closeBookingInfo(oneEventDiv);
                    } else {
                        showBookingInfo(event, oneEventDiv);
                    }
            };

            const yearOfEvent = event.date.substring(yearInDateDigit, yearInDateDigit+4);
            const monthOfEvent = event.date.substring(monthInDateDigit, monthInDateDigit + 2);
            const dayOfEvent = event.date.substring(dayInDateDigit, dayInDateDigit + 2);
            const dayOfWeekOfEvent = getDayOfWeek(event.date.substring(0, 10));
            const startDateOfEvent = event.date.substring(timeInDateDigit, timeInDateDigit + 5);
            if(!eventMonthsAndYears.includes(monthOfEvent.toString() + yearOfEvent.toString())){
                const monthWithLetters = getMonthName(monthOfEvent);
                monthDividerDiv.innerHTML = `
                <div class="month-divider"> 
                 ${monthWithLetters} ${yearOfEvent}
                </div>
                `
                eventMonthsAndYears.push(monthOfEvent.toString() + yearOfEvent.toString());
                showBookingsDiv.appendChild(monthDividerDiv)
            }

            oneEventDiv.innerHTML = `
            <div class="event-header">
                <div class="date-and-day-of-the-week">
                    <div id="date-of-event">${dayOfEvent}</div>
                    <div id="day-of-the-week">${dayOfWeekOfEvent}</div>
                </div>
                <div class="time-of-event">${startDateOfEvent} - ${calculateEndTime(startDateOfEvent, event.duration)}
                </div>
                <div class="name-of-event">
                    <b>${event.name}</b>
                    <br>
                </div>
            </div>
        `;
            showBookingsDiv.appendChild(oneEventDiv);
    });
}

/**
 * Shows a pop-up with the booking info of the selected event.
 * @param event the selected event
 * @param oneEventDiv div representing the selected event
 */
function showBookingInfo(event, oneEventDiv) {
    currentSelectedEvent = event;
    const existingInfoContainer = oneEventDiv.querySelector('.info-container');
    if (existingInfoContainer) {
        existingInfoContainer.remove(); // Remove existing info container if it exists
    }

    const infoContainer = document.createElement('div');
    infoContainer.className = 'info-container';

    const infoDetails = document.createElement('div');
    infoDetails.className = 'info-details';

    const eventDate = event.eventDate ? event.eventDate.split(' ') : ['N/A', 'N/A'];
    const date = eventDate[0];
    const time = eventDate[1];

    infoDetails.innerHTML = `
        <div id="booking-info-container">
            <div id="title-booking-info" xmlns="http://www.w3.org/1999/html">Booking info</div>
            <div id = "number-of-enrolled"> ${event.currentmembers}/${event.maxmembers} enrolled</div>
        </div>
        <div class="event-info-row"> 
            <div class = "event-detail-item"><div class="event-detail-title">ID </div><div class = "event-detail-data"> ${event.eid}</div></div>
            <div class = "event-detail-item"><div class="event-detail-title">Client </div><div class = "event-detail-data"> ${event.clientName}</div></div>
        </div>
        <div class="event-info-row"> 
            <div class = "event-detail-item"><div class="event-detail-title">Booking type </div><div class = "event-detail-data"> ${event.bookingtype}</div></div>
            <div class = "event-detail-item"><div class="event-detail-title">Location </div><div class = "event-detail-data"> ${event.location}</div></div>
        </div>
    `;

    if (event.eventType === 'Completed') {
        infoDetails.innerHTML += '<p class="event-full">Event Completed</p>';
    }

    const completedBox = document.createElement('div');
    completedBox.className = 'completed-events-box';

    if (event.status == 'COMPLETED'){
        completedBox.innerHTML = `
        <container style="display: flex; justify-content: center; align-items: center; ">
            <div class="completed-events-box" style="color: #4CAF50; justify-content: center;">This event is completed.</div>
        </container>
    `;
    } else {
        completedBox.innerHTML = `
        <container style="display: flex; justify-content: center; align-items: center; ">
            <div class="completed-events-box" style="color: blueviolet; text-align: center;">This event is upcoming.</div>
        </container>
    `;
    }

    infoContainer.appendChild(infoDetails);
    infoContainer.appendChild(completedBox);
    infoContainer.style.opacity = '0';

    setTimeout(() => {
        oneEventDiv.appendChild(infoContainer);
        infoContainer.style.opacity = '1';
    }, 10);

    oneEventDiv.classList.add('expanded');
}


/**
 * Closes the booking info pop-up.
 * @param oneEventDiv div representing the selected event
 */
function closeBookingInfo(oneEventDiv) {
    const infoDetails = oneEventDiv.querySelector('.info-details');
    const buttonGroup = oneEventDiv.querySelector('.button-group');

    oneEventDiv.addEventListener('transitioned', function(event) {
        if (event.propertyName === 'max-height') {
            if (infoDetails) {
                infoDetails.remove();
            }
            if (buttonGroup) {
                buttonGroup.remove();
            }
        }
    });

    oneEventDiv.classList.remove('expanded');
}

function getMonthName(month) {
    switch (month) {
        case '01':
            return 'January';
        case '02':
            return 'February';
        case '03':
            return  'March';
        case '04':
            return  'April';
        case '05':
            return 'May';
        case '06':
            return 'June';
        case '07':
            return 'July';
        case '08':
            return 'August';
        case '09':
            return 'September';
        case '10':
            return  'October';
        case '11':
            return 'November';
        case '12':
            return 'December';
        default:
            return 'Invalid month number';
    }
}

function getDayOfWeek(dateString) {
    const [year, month, day] = dateString.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const dayOfWeek = date.getDay();
    return daysOfWeek[dayOfWeek];
}

function calculateEndTime(startTime, duration) {
    const [hour, minutes] = startTime.split(':').map(Number);
    let endHour = hour + duration;
    if (endHour >= 24) {endHour -= 24;}
    const formattedEndHour = endHour < 10 ? '0' + endHour : '' + endHour;
    const formattedMinutes = minutes < 10 ? '0' + minutes : '' + minutes;
    return formattedEndHour + ":" + formattedMinutes;
}

function logOut() {
    sessionStorage.removeItem('token');
}