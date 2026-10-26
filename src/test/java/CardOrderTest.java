import io.github.bonigarcia.wdm.WebDriverManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardOrderTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private final By nameField =
            By.cssSelector("[data-test-id='name'] input");

    private final By phoneField =
            By.cssSelector("[data-test-id='phone'] input");

    private final By agreementCheckbox =
            By.cssSelector("[data-test-id='agreement'] .checkbox__box");

    private final By agreementInput =
            By.cssSelector("[data-test-id='agreement'] input");

    private final By continueButton =
            By.cssSelector("button");

    private final By successMessage =
            By.cssSelector("[data-test-id='order-success']");

    private final By nameError =
            By.cssSelector("[data-test-id='name'].input_invalid");

    private final By phoneError =
            By.cssSelector("[data-test-id='phone'].input_invalid");

    private final By agreementError =
            By.cssSelector("[data-test-id='agreement'].input_invalid");


    @BeforeAll
    public static void setupAll() {
        WebDriverManager.chromedriver().setup();
    }


    @BeforeEach
    public void setup() {

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);

        wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(10)
        );

        driver.get("http://localhost:9999");
    }


    @AfterEach
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }


    // Заполнение полей формы
    private void fillForm(String name, String phone) {

        driver.findElement(nameField).sendKeys(name);
        driver.findElement(phoneField).sendKeys(phone);
    }


    // Установка согласия
    private void setAgreement() {

        wait.until(
                ExpectedConditions.elementToBeClickable(
                        agreementCheckbox
                )
        ).click();

        assertTrue(
                driver.findElement(agreementInput).isSelected(),
                "Согласие на обработку данных не установлено"
        );
    }


    // Отправка формы
    private void submitForm() {

        wait.until(
                ExpectedConditions.elementToBeClickable(
                        continueButton
                )
        ).click();
    }


    // Ожидание сообщения об ошибке
    private void checkError(By errorLocator) {

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        errorLocator
                )
        );
    }


    // ТЕСТ 1
    // Успешная отправка заявки
    @Test
    public void shouldSubmitFormWithValidData() {

        fillForm("Иванов Иван", "+79991234567");

        setAgreement();

        submitForm();

        WebElement success = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        successMessage
                )
        );

        String actualText = success.getText();

        assertTrue(
                actualText.contains("Ваша заявка успешно отправлена!"),
                "Неверный текст сообщения: " + actualText
        );
    }


    // ТЕСТ 2
    // Пустое поле имени
    @Test
    public void shouldShowErrorWhenNameIsEmpty() {

        fillForm("", "+79991234567");

        setAgreement();

        submitForm();

        checkError(nameError);
    }


    // ТЕСТ 3
    // Пустое поле телефона
    @Test
    public void shouldShowErrorWhenPhoneIsEmpty() {

        fillForm("Иванов Иван", "");

        setAgreement();

        submitForm();

        checkError(phoneError);
    }


    // ТЕСТ 4
    // Оба поля пустые
    @Test
    public void shouldShowErrorWhenAllFieldsAreEmpty() {

        setAgreement();

        submitForm();

        checkError(nameError);
    }


    // ТЕСТ 5
    // Имя латинскими буквами
    @Test
    public void shouldShowErrorForLatinName() {

        fillForm("Ivan Ivanov", "+79991234567");

        setAgreement();

        submitForm();

        checkError(nameError);
    }


    // ТЕСТ 6
    // Имя содержит цифры
    @Test
    public void shouldShowErrorForNameWithNumbers() {

        fillForm("Иван123", "+79991234567");

        setAgreement();

        submitForm();

        checkError(nameError);
    }


    // ТЕСТ 7
    // Имя содержит специальные символы
    @Test
    public void shouldShowErrorForNameWithSpecialCharacters() {

        fillForm("Иван@Иванов", "+79991234567");

        setAgreement();

        submitForm();

        checkError(nameError);
    }


    // ТЕСТ 8 и 9
    // Неправильная длина телефона
    @ParameterizedTest
    @ValueSource(strings = {
            "+7999123456",
            "+799912345678"
    })
    public void shouldShowErrorForInvalidPhoneLength(String phone) {

        fillForm("Иванов Иван", phone);

        setAgreement();

        submitForm();

        checkError(phoneError);
    }


    // ТЕСТ 10
    // Телефон без знака +
    @Test
    public void shouldShowErrorForPhoneWithoutPlus() {

        fillForm("Иванов Иван", "79991234567");

        setAgreement();

        submitForm();

        checkError(phoneError);
    }


    // ТЕСТ 11
    // Не установлено согласие
    @Test
    public void shouldShowErrorWithoutAgreement() {

        fillForm("Иванов Иван", "+79991234567");

        submitForm();

        checkError(agreementError);
    }
}