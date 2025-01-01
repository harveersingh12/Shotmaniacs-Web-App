document.addEventListener("DOMContentLoaded", function () {
    const dateInput = document.getElementById('eventDate');
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0'); // Months are zero-based
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');

    const formattedDate = `${year}-${month}-${day}T${hours}:${minutes}`;

    dateInput.min = formattedDate;

    var sliderContainer = document.querySelector(".slider-container");
    var slider = sliderContainer.querySelector(".slider");
    var slides = slider.querySelectorAll(".slide");
    var slideIndex = 0;
    var slideCount = slides.length-1;

    function updateSlider() {
        var slideWidth = slides[0].offsetWidth ;
        slider.style.transform = "translateX(" + (-slideIndex * slideWidth) + "px)";
    }

    sliderContainer.querySelector(".next").addEventListener("click", function () {
        slideIndex = (slideIndex + 1) % slideCount;
        updateSlider();
    });

    sliderContainer.querySelector(".prev").addEventListener("click", function () {
        slideIndex = (slideIndex - 1 + slideCount) % slideCount;
        updateSlider();
    });

    window.addEventListener("resize", updateSlider);
    updateSlider();
});



function sendEmail(eventData) {
    fetch('/shotmaniacs_war/api/email/newbooking', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(eventData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to notify admin');
            }
        })
        .catch(error => {
            console.error('Error notifying admin:', error);
        });
}

document.getElementById('eventForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const data = {
        eventName: formData.get('eventName'),
        eventType: formData.get('eventType'),
        eventDate: formData.get('eventDate'),
        eventLocation: formData.get('eventLocation'),
        eventDuration: formData.get('eventDuration'),
        clientName: formData.get('clientName'),
        clientEmail: formData.get('clientEmail'),
        status: 'ONGOING'
    };

    fetch('/shotmaniacs_war/api/event', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(responseData => {
            showPopup('Event successfully sent', 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            showPopup('There was a error sending the event', 'error');
        });
    sendEmail(data);

    document.getElementById('eventForm').reset();
});

document.getElementById('filesubmission').addEventListener('submit', function(e) {
    e.preventDefault();

    // Get the uploaded file
    const fileInput = document.getElementById('fileupload');
    const file = fileInput.files[0];

    if (file) {
        // Read the uploaded file
        const reader = new FileReader();
        reader.onload = async function(event) {
            const data = new Uint8Array(event.target.result);
            const workbook = XLSX.read(data, { type: 'array' });

            //taking the first sheet that needs to be processed
            const sheetName = workbook.SheetNames[0];
            const sheet = workbook.Sheets[sheetName];

            //convert the sheet to JSON format
            const jsonData = XLSX.utils.sheet_to_json(sheet, { header: 1 });

            // Process each row of data
            const token = sessionStorage.getItem('token');
            const headers = {
                'Content-Type': 'application/json'
            };
            if (token) {
                headers['Authorization'] = 'Bearer ' + token;
            }

            for (const row of jsonData.slice(1)) {
                const formData = {
                    eventName: row[0],
                    eventType: row[1],
                    eventDate: row[2],
                    eventLocation: row[3],
                    eventDuration: row[4],
                    clientName: row[5],
                    clientEmail: row[6]
                };

                try {
                    const response = await fetch('/shotmaniacs_war/api/event', {
                        method: 'POST',
                        body: JSON.stringify(formData),
                        headers: headers
                    });

                    if (!response.ok) {
                        throw new Error(`Error: ${response.statusText}`);
                    }

                    const responseData = await response.json();
                    showPopup('Event successfully sent', 'success');
                } catch (error) {
                    console.error('Error:', error);
                    showPopup('There was an error sending the event', 'error');
                }
            }

            document.getElementById('filesubmission').reset();
        };
        reader.readAsArrayBuffer(file);
    } else {
        console.error('No file selected.');
    }
});

