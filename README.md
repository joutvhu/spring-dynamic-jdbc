# Spring Dynamic JDBC

The Spring Dynamic JDBC will make it easy to implement dynamic queries with Spring Data JDBC.

## How to use?

### Install dependency

```groovy
implementation 'com.github.joutvhu:spring-dynamic-jdbc:3.0.0'
```

```xml
<dependency>
    <groupId>com.github.joutvhu</groupId>
    <artifactId>spring-dynamic-jdbc</artifactId>
    <version>3.0.0</version>
</dependency>
```

- Please choose the _Spring Dynamic JDBC_ version appropriate with your spring version.

  | Spring Boot version | Spring Dynamic JDBC version |
  |:----------:|:-------------:|
  | 3.0.x | 3.0.0 |

Also, you have to choose a [Dynamic Query Template Provider](https://github.com/joutvhu/spring-dynamic-commons#dynamic-query-template-provider) to use,
the Dynamic Query Template Provider will decide the style you write dynamic query template.

In this document, I will use [Spring Dynamic Freemarker](https://github.com/joutvhu/spring-dynamic-freemarker).

```groovy
implementation 'com.github.joutvhu:spring-dynamic-freemarker:1.0.0'
```

```xml
<dependency>
    <groupId>com.github.joutvhu</groupId>
    <artifactId>spring-dynamic-freemarker</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Configuration

- First you need to create a bean of `DynamicQueryTemplateProvider`, that depending on which the Dynamic Query Template Provider you are using.

```java
@Bean
public DynamicQueryTemplateProvider dynamicQueryTemplateProvider() {
    FreemarkerQueryTemplateProvider provider = new FreemarkerQueryTemplateProvider();
    provider.setTemplateLocation("classpath:/query");
    provider.setSuffix(".dsql");
    return provider;
}
```

- Next, you need to set the jdbc repository's `repositoryFactoryBeanClass` property to `DynamicJdbcRepositoryFactoryBean.class`.

```java
// Config with annotation
@EnableJdbcRepositories(repositoryFactoryBeanClass = DynamicJdbcRepositoryFactoryBean.class)
```

### Dynamic query

- Use annotation @DynamicQuery to define dynamic queries.

```java
public interface UserRepository extends CrudRepository<User, Long> {
    @DynamicQuery(
        value = "select * from USER where FIRST_NAME = :firstName\n" +
            "<#if lastName?has_content>\n" +
            "  and LAST_NAME = :lastName\n" +
            "</#if>"
    )
    List<User> findUserByNames(Long firstName, String lastName);

    @Query(value = "select * from USER where FIRST_NAME = :firstName")
    List<User> findByFirstName(String firstName);

    @DynamicQuery(
        value = "select USER_ID from USER\n" +
            "<#if name??>\n" +
            "  where concat(FIRST_NAME, ' ', LAST_NAME) like %:name%\n" +
            "</#if>"
    )
    List<Long> searchIdsByName(String name);

    @DynamicQuery(
        value = "select * from USER\n" +
            "<#if role??>\n" +
            "  where ROLE = :role\n" +
            "</#if>"
    )
    List<User> findByRole(String role);
}
```

### Load query template files

- If you do not specify the query template on the `@DynamicQuery` annotation.
  The `DynamicQueryTemplateProvider` will find them from external template files based on the `TemplateLocation` and `Suffix` that you specify in the provider.

- If you don't want to load the template from external template files you can use the following code `provider.setSuffix(null);`.

- Each template will start with a template name definition line. The template name definition line must be start with two dash characters (`--`). The template name will have the following syntax.

  ```
  entityName:methodName
  ```

- Query templates (Ex: `resoucers/query/user-query.dsql`) 

```sql
--User:findUserByNames
select * from USER where FIRST_NAME = :firstName
<#if lastName?has_content>
  and LAST_NAME = :lastName
</#if>

-- User:searchIdsByName
select USER_ID from USER
<#if name??>
  where concat(FIRST_NAME, ' ', LAST_NAME) like %:name%
</#if>

-- User:findByRole
select * from USER
<#if role??>
  where ROLE = :role
</#if>

-- User:findByGroup
select * from USER
<#if group.name?starts_with("Git")>
  where GROUP_ID = :#{#group.id}
</#if>
```

- Now you don't need to specify the query template on `@DynamicQuery` annotation.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    @DynamicQuery
    List<User> findUserByNames(Long firstName, String lastName);

    @Query(value = "select * from USER where FIRST_NAME = :firstName")
    List<User> findByFirstName(String firstName);

    @DynamicQuery
    List<Long> searchIdsByName(String name);

    @DynamicQuery
    List<User> findByRole(String role);

    @DynamicQuery
    List<User> findByGroup(Group group);
}
```
