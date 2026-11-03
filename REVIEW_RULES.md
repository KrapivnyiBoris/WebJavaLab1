# Критерії Код-Рев'ю та Архітектурні Вимоги Викладачки (Lab 1: Cosmo Cats)

Цей документ містить повний перелік правил, вимог та особистих преференцій викладачки курсу (за матеріалами 6 лекцій та запису публічного код-рев'ю Лабораторної №1).

## 1. Архітектура та структура пакетів (Clean / Onion Architecture)
1. **Організація пакетів за шарами (функцією класів), а не за доменними фічами:**
   - Не змішувати підходи (не розкидати DTO чи репозиторії по різних папках домену).
   - Не використовувати складні назви на кшталт `infrastructure.persistence` (слово `infrastructure` заборонено).
   - Бажана структура пакетів:
     - `web` (або `controller`) — REST-контролери, `@RestControllerAdvice` та утилітні класи для веб-помилок.
     - `dto` — усі Request/Response DTO в одному пакеті.
     - `mapper` — інтерфейси та реалізації маперів.
     - `service` — інтерфейси сервісів (`ProductService`) та їхні реалізації (`ProductServiceImpl`).
     - `domain` — доменні моделі (`Product`, `Category`, `Order`, `Cart`, `OrderItem`, `CartItem`, `OrderStatus`).
     - `repository` (якщо виділено шар in-memory сховища) — інтерфейси та реалізації репозиторіїв, а також окремий `final` клас зі стартовими mock-даними (без зайвого приватного конструктора, якщо це не `@UtilityClass`).
     - `exception` — кастомні винятки.
     - `validation` — кастомні анотації валідації, валідатори та маркерні інтерфейси груп валідації.
2. **Сувора ізоляція шарів (Separation of Concerns):**
   - **Контролер** — максимально «тупий маршрутизатор». Приймає DTO, валідує вхідні дані, викликає мапер для перетворення `DTO -> Domain`, передає доменний об'єкт у сервіс, а результат із сервісу мапить назад у `Domain -> DTO` і загортає в `ResponseEntity`.
   - **Сервіс** працює ВИКЛЮЧНО з доменними об'єктами (`Product`, `Category` тощо). Сервіс категорично не повинен приймати або повертати `ProductDto` чи `PagedProductResponseDto` (протікання DTO в сервісний шар — груба помилка!).
3. **Обов'язковий інтерфейс для сервісу:**
   - Має бути інтерфейс `ProductService` та клас реалізації `ProductServiceImpl` з анотацією `@Service`. Контролер має інжектити саме інтерфейс `ProductService`.

## 2. Іменування та читабельність («Код має читатися як книжка»)
1. **Заборона коротких безконтекстних назв методів (типова помилка AI):**
   - Категорично не називати методи у контролерах, сервісах чи репозиторіях просто `list()`, `getAll()`, `getById()`, `create()`, `update()`, `delete()`.
   - Назви мають бути семантично повними: `getAllProducts()`, `getProductById()`, `createProduct()`, `updateProduct()`, `deleteProductById()`.
2. **Іменування полів та констант:**
   - Поля залежностей називати інформативно (`productService`, `productMapper`, а не просто `service` чи `mapper`).
   - Усі константи (`static final`) писати у форматі `UPPER_SNAKE_CASE`.
3. **Чистота умовних конструкцій та функціональний стиль:**
   - Уникати складних тернарних операторів (`? :`) та нагромадження `if-else`.
   - Використовувати `Optional`, `Stream API`, лямбди та method chaining (`.filter(...).findFirst().orElseThrow(...)`).
   - Не робити конкатенацію рядків через `+` у циклах.
4. **Мова коментарів та документації:**
   - Усі коментарі в коді, Javadoc та описи в `product-api.yml` мають бути виключно англійською мовою. Короткі змістовні Javadoc над класами вітаються.

## 3. Dependency Injection та Багатопотоковість (Thread Safety)
1. **Тільки Constructor Injection + `private final`:**
   - Жодних `@Autowired` на полях (Field Injection) чи сеттерах. Усі залежності оголошуються як `private final` і передаються через конструктор (або `@RequiredArgsConstructor`).
2. **Потокобезпечність Singleton-бінів:**
   - Не зберігати мутабельний стан запиту в полях сервісу.
   - Для in-memory сховища використовувати `ConcurrentHashMap` та `UUID` (не використовувати звичайний `HashMap`, `ArrayList` чи неатомарний лічильник `long++`).

## 4. Вимоги до DTO та Доменних Моделей (DDD)
1. **DTO мають бути суворо Immutable:**
   - Використовувати анотації Lombok `@Value`, `@Builder`, `@Jacksonized` (або Java `record`, але викладачка надає перевагу `@Value` + `@Builder` + `@Jacksonized`). У DTO не повинно бути сеттерів!
2. **Розділення Request DTO та Response DTO (Заборона передачі `id` клієнтом):**
   - КРИТИЧНО: Не використовувати один і той самий `ProductDto` з полем `id` і для запиту (`POST`/`PUT`), і для відповіді! Клієнт не повинен мати змоги передати або перезаписати `id` у тілі запиту. Має бути окремий `ProductRequestDto` (без поля `id`, з валідаціями) та `ProductResponseDto` (з `id`).
3. **Типи даних:**
   - Для грошей (`price`) — тільки `BigDecimal` (ніколи не `Double`/`Float`).
   - Для дат — тільки типи з `java.time` (`Instant`, `LocalDateTime`), ніколи не `java.util.Date`.
4. **Доменні об'єкти (`Product`, `Category`, `Order`, `Cart`):**
   - Використовувати Lombok (`@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor`).
   - Доменні об'єкти (на відміну від DTO) мають бути мутабельними.
   - Вітається Rich Domain Model: бізнес-методи всередині доменних класів, які працюють із власними полями сутності (наприклад, розрахунок суми в `Order`, `checkout()` у `Cart`, `CartItem` з кількістю, `enum OrderStatus`).
   - НЕ писати валідацію з викиданням винятків у конструкторі доменного об'єкта `Product` (валідація має відбуватися на рівні DTO або в сервісі/мапері).

## 5. Мапери (Ручний мапер + Бонус MapStruct)
1. **Жодних статичних методів:** Ручний мапер (`ProductManualMapper`) має бути Spring-компонентом (`@Component`), без `static` методів.
2. **Перевірки на `null`:** У ручному мапері обов'язково перевіряти вхідний аргумент `if (source == null) return null;` та будувати об'єкти через `.builder()...build()`.
3. **MapStruct:** Інтерфейс `ProductMapper` з `@Mapper(componentModel = "spring")`. Обидва мапери можуть імплементувати спільний інтерфейс з вирішенням колізії бінів через `@Primary` / `@Qualifier`.

## 6. Контролер, Валідація та Пагінація
1. **`ResponseEntity<...>` та HTTP-статуси:**
   - Усі методи контролера повертають явно типізований `ResponseEntity<...>`.
   - `POST` повертає `201 Created` із заголовком `Location`.
   - `DELETE` повертає `204 No Content` (ідемпотентний).
2. **Анотації валідації:**
   - Над класом `ProductController` ОБОВ'ЯЗКОВО має стояти `@Validated`.
   - На вхідних `@RequestBody` має стояти `@Valid`.
3. **Кастомна анотація `@CosmicWordCheck` та групи валідації:**
   - `@Target` анотації `@CosmicWordCheck` має бути обмежений полями/параметрами (`FIELD`, `PARAMETER`, `TYPE_USE`), без `ElementType.TYPE` (не можна вішати на клас).
   - Бажано використати групу валідації (маркерний інтерфейс `ExtendedValidation` та `@GroupSequence`), щоб `@CosmicWordCheck` виконувався лише тоді, коли базова перевірка `@NotBlank` пройшла успішно.
4. **Пагінація:**
   - Параметри `page` (`@Min(0)`, default 0) та `size` (`@Min(1)`, `@Max(100)`, default 10).
   - Обгортка пагінації (наприклад, узагальнений `PageResponseDto<T>` або `PagedProductResponseDto`) має містити `content`, `page`, `size`, `totalElements`, `totalPages`.
   - Оскільки сервіс не повинен знати про DTO, сервіс має повертати доменну обгортку сторінки (наприклад, `PageResult<Product>`), а контролер/мапер перетворює її на DTO-обгортку!
   - Перед `.skip().limit()` дані з `ConcurrentHashMap` обов'язково мають сортуватися (наприклад, за `id` або `name`), щоб порядок на сторінках був детермінованим.

## 7. Обробка помилок (RFC 9457), Ексепшени та Логування (`@Slf4j`)
1. **Заборона загального `NotFoundException`:**
   - Ексепшени мають бути гранулярними: `ProductNotFoundException`, `CategoryNotFoundException` тощо. Наслідують `RuntimeException`.
2. **Шаблон повідомлення ВСЕРЕДИНІ класу винятку:**
   - Не передавати сирий текст помилки з сервісу (`throw new ProductNotFoundException("Product ...")` — заборонено!).
   - Усередині `ProductNotFoundException` має бути константа `private static final String PRODUCT_NOT_FOUND_MESSAGE = "Product with id %s not found";`, а конструктор приймає тільки `UUID id`.
3. **Побудова `@RestControllerAdvice` (`GlobalExceptionHandler`):**
   - Клас має наслідувати `ResponseEntityExceptionHandler` і перевизначати `handleMethodArgumentNotValid(...)`.
   - Не додавати зайвих/недоречних хендлерів з `ResponseEntityExceptionHandler` (наприклад, `handleTypeMismatch` для `BeansException` або `handleHandlerMethodValidationException`, якщо він перекривається іншим).
   - Назви методів-хендлерів мають починатися зі слова `handle...Exception` (наприклад, `handleProductNotFoundException`).
   - Використовувати стандартний `org.springframework.http.ProblemDetail` (не створювати власний клас ErrorResponse).
   - Винести формування `ProblemDetail` в окремий утилітний клас із анотацією Lombok `@UtilityClass` (наприклад, `ProblemDetailsUtils`).
   - Поле `detail` для валідації має містити точний формат: `"Field '<field>' <message>."`, `title` = `"Validation failed"`, `type` = `"https://cosmo-cats.market/errors/validation"`.
4. **Логування (`@Slf4j`):**
   - Обов'язкове логування в `ProductServiceImpl` та `GlobalExceptionHandler`.
   - У `GlobalExceptionHandler` усі штатно оброблені клієнтські помилки (400, 404, 409) логувати через `log.info(...)` (НЕ `log.error(...)`!). `log.error` — тільки для непередбачуваних 500-х помилок.
5. **Не використовувати винятки як boolean-перевірку:**
   - Приватні методи перевірки (наприклад, перевірка існування категорії) мають або повертати `boolean` / `Optional`, або використовуватися прозоро через `repository.findBy...().orElseThrow(...)`, без заплутаних вкладень.

## 8. OpenAPI Специфікація (`product-api.yml`) та `.gitignore`
1. **`product-api.yml`:**
   - Повна відповідність реальному коду, включаючи розділення на `ProductRequest` (без `id`) та `ProductResponse` (з `id`).
   - Обов'язкова наявність коду `404 Not Found` (зі схемою `ProblemDetail`) не тільки на `GET/PUT/DELETE /api/v1/products/{id}`, а й на `GET /api/v1/products` (вимога викладачки з live-рев'ю).
   - Наявність `minimum`, `maximum`, `default` на параметрах пагінації та всіх обмежень полів.
2. **`.gitignore`:**
   - Обов'язково має ігнорувати `gradle-wrapper.jar` (а також `.gradle/`, `build/`, `.idea/`, `REVIEW_RULES.md`), щоб скомпільований `.jar` врапера не потрапив у GitHub PR!