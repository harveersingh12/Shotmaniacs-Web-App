let popupVisible = false;

function showPopup(message, type) {
    if (popupVisible) return;
    popupVisible = true;
    const popup = document.createElement('div');
    popup.classList.add('popup', type);
    popup.textContent = message;
    popup.style.opacity = '0'; // Initial opacity set to 0
    document.body.appendChild(popup);

    setTimeout(() => {
        popup.style.opacity = '1'; // Make the popup visible
    }, 10);

    setTimeout(() => {
        popup.classList.add('fade-out');
    }, 4500);

    setTimeout(() => {
        document.body.removeChild(popup);
        popupVisible = false;
    }, 5000);
}