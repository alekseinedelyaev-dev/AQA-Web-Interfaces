
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardOrderTest {

    // Точные тексты сообщений банковского приложения
    private static final String REQUIRED_ERROR =
            "Поле обязательно для заполнения";

    private static final String NAME_FORMAT_ERROR =
            "Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.";

    private static final String PHONE_FORMAT_ERROR =
            "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";

    private static final String SUCCESS_TEXT =
            "Ваша заявка успешно отправлена!";

    private WebDriver driver;
    private WebDriverWait wait;

    // Локаторы элементов страницы
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


    // Настройка ChromeDriver
    @BeforeAll
    public static void setupAll() {
        WebDriverManager.chromedriver().setup();
    }


    // Открываем браузер перед каждым тестом
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


    // Закрываем браузер после каждого теста
    @AfterEach
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }


    // Заполняем поля формы
    private void fillForm(String name, String phone) {

        driver.findElement(nameField).sendKeys(name);

        driver.findElement(phoneField).sendKeys(phone);
    }


    // Устанавливаем согласие на обработку данных
    private void setAgreement() {

        wait.until(
                ExpectedConditions.elementToBeClickable(
                        agreementCheckbox
                )
        ).click();
    }


    // Нажимаем кнопку "Продолжить"
    private void submitForm() {

        wait.until(
                ExpectedConditions.elementToBeClickable(
                        continueButton
                )
        ).click();
    }


    // Проверяем появление ошибки и её текст
    private void checkError(By errorLocator, String expectedText) {

        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        errorLocator
                )
        );

        String actualText = error.getText();

        assertTrue(
                actualText.contains(expectedText),
                "Неверный текст ошибки."
                        + "\nОжидалось: " + expectedText
                        + "\nФактически: " + actualText
        );
    }


    // ТЕСТ 1
    // Успешная отправка заявки с корректными данными
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
                actualText.contains(SUCCESS_TEXT),
                "Неверный текст сообщения об успешной отправке: "
                        + actualText
        );
    }


    // ТЕСТ 2
    // Пустое поле имени
    @Test
    public void shouldShowErrorWhenNameIsEmpty() {

        fillForm("", "+79991234567");

        setAgreement();

        submitForm();

        checkError(nameError, REQUIRED_ERROR);
    }


    // ТЕСТ 3
    // Пустое поле телефона
    @Test
    public void shouldShowErrorWhenPhoneIsEmpty() {

        fillForm("Иванов Иван", "");

        setAgreement();

        submitForm();

        checkError(phoneError, REQUIRED_ERROR);
    }


    // ТЕСТ 4
    // Оба поля пустые
    @Test
    public void shouldShowErrorWhenAllFieldsAreEmpty() {

        setAgreement();

        // Первая отправка с двумя пустыми полями
        submitForm();

        // Сначала приложение проверяет поле имени
        checkError(nameError, REQUIRED_ERROR);

        // Заполняем имя, оставляя телефон пустым
        driver.findElement(nameField).sendKeys("Иванов Иван");

        // Повторно отправляем форму
        submitForm();

        // Теперь проверяем текст ошибки телефона
        checkError(phoneError, REQUIRED_ERROR);
    }


    // ТЕСТ 5
    // Имя латинскими буквами
    @Test
    public void shouldShowErrorForLatinName() {

        fillForm("Ivan Ivanov", "+79991234567");

        setAgreement();

        submitForm();

        checkError(nameError, NAME_FORMAT_ERROR);
    }


    // ТЕСТ 6
    // Имя содержит цифры
    @Test
    public void shouldShowErrorForNameWithNumbers() {

        fillForm("Иван123", "+79991234567");

        setAgreement();

        submitForm();

        checkError(nameError, NAME_FORMAT_ERROR);
    }


    // ТЕСТ 7
    // Имя содержит специальные символы
    @Test
    public void shouldShowErrorForNameWithSpecialCharacters() {

        fillForm("Иван@Иванов", "+79991234567");

        setAgreement();

        submitForm();

        checkError(nameError, NAME_FORMAT_ERROR);
    }


    // ТЕСТЫ 8 И 9
    // Номер телефона содержит неправильное количество цифр
    @ParameterizedTest
    @ValueSource(strings = {
            "+7999123456",
            "+799912345678"
    })
    public void shouldShowErrorForInvalidPhoneLength(String phone) {

        fillForm("Иванов Иван", phone);

        setAgreement();

        submitForm();

        checkError(phoneError, PHONE_FORMAT_ERROR);
    }


    // ТЕСТ 10
    // Телефон без знака плюс
    @Test
    public void shouldShowErrorForPhoneWithoutPlus() {

        fillForm("Иванов Иван", "79991234567");

        setAgreement();

        submitForm();

        checkError(phoneError, PHONE_FORMAT_ERROR);
    }


    // ТЕСТ 11
    // Отсутствие согласия на обработку данных
    @Test
    public void shouldShowErrorWithoutAgreement() {

        fillForm("Иванов Иван", "+79991234567");

        submitForm();

        // Приложение выделяет чекбокс ошибкой,
        // но не показывает отдельное текстовое сообщение
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        agreementError
                )
        );

        // Проверяем, что согласие не установлено
        assertFalse(
                driver.findElement(agreementInput).isSelected(),
                "Согласие не должно устанавливаться автоматически"
        );
    }
}
