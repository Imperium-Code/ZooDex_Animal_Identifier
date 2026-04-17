document.addEventListener('DOMContentLoaded', () => {
    const loadingScreen = document.getElementById('loading-screen');
    const mainUi = document.getElementById('main-ui');
    const scanBtn = document.getElementById('scan-btn');
    const scannerLine = document.getElementById('scanner-line');
    const animalName = document.getElementById('animal-name');
    const animalDesc = document.getElementById('animal-desc');
    const typeContainer = document.querySelector('.type-container');

    // Simulate loading time 
    setTimeout(() => {
        loadingScreen.classList.add('hidden');
        setTimeout(() => {
            loadingScreen.style.display = 'none';
            mainUi.classList.remove('hidden');
        }, 1000); // Wait for CSS transition
    }, 3000); // Initial 3 second boot sequence

    scanBtn.addEventListener('click', () => {
        // Prevent multiple scans concurrently
        if (scannerLine.style.display === 'block') return;

        // Start scanning animation
        scannerLine.style.display = 'block';
        scannerLine.style.animation = 'scanAnim 2s linear infinite';
        
        animalName.textContent = "SCANNING...";
        animalDesc.textContent = "Analyzing structural data...";
        typeContainer.innerHTML = '';

        // Simulate a delay fetching data from local backend module
        setTimeout(() => {
            scannerLine.style.display = 'none';
            scannerLine.style.animation = 'none';

            // Mock response (This is where the Raspberry Pi offline AI model responds)
            const mockData = {
                name: "Red Panda",
                type: "MAMMAL",
                description: "A small arboreal mammal native to the eastern Himalayas. It has reddish-brown fur and a long, shaggy tail."
            };

            animalName.textContent = mockData.name;
            typeContainer.innerHTML = `<span class="type-badge">TYPE: ${mockData.type}</span>`;
            
            // Typewriter effect to display description
            animalDesc.textContent = '';
            let i = 0;
            const typeWriter = () => {
                if (i < mockData.description.length) {
                    animalDesc.textContent += mockData.description.charAt(i);
                    i++;
                    setTimeout(typeWriter, 20); // 20ms per character
                }
            };
            typeWriter();

        }, 4000);
    });
});
