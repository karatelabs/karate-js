function generateCardNumber(firstSix, length) {

    function luhnCheck(input) {
        const number = input.toString();
        const digits = number.replace(/\D/g, '').split('').map(Number);
        let sum = 0;
        let isSecond = false;
        for (let i = digits.length - 1; i >= 0; i--) {
            let digit = digits[i];
            if (isSecond) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            isSecond = !isSecond;
        }
        return sum % 10;
    }

    function randomDigit() {
        return Math.floor(Math.random() * 9);
    }

    let cardNumber = firstSix;
    while (cardNumber.length < length - 1) {
        cardNumber = cardNumber + randomDigit();
    }
    cardNumber = cardNumber + '9';
    let luhnVal = luhnCheck(cardNumber);
    cardNumber = cardNumber - luhnVal;
    return cardNumber.toString();

}

generateCardNumber('411111',16);