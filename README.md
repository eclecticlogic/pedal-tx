Pedal
=====

A Java 8 based idiomatic JPA DAO framework. Let the examples say the rest.

# Feature Highlights

- Advanced transaction management
	- Lambda based transaction boundary
	- Transaction attached objects
	- Transaction attached jobs
- DAO shell with predefined functions
    - Fluent interface for HQL/JQL queries
    - Simple integration with QueryDSL	
    - Hooks to set insert/update date/time automatically
    - DAO registry function for dynamic CRUD operations
- Postgresql specific features
	- Support for COPY to insert data using collection of JPA entities
	- Hibernate user-types for Postgres arrays
	- Hibernate user-type for Postgres bit array  
- Data loader support
	- Simple groovy DSL for defining test data to be loaded
	- Works at the JPA entity level (not database columns and values but JPA entity properties and "converted" types). 
	 
# Getting started

Download the Pedal jar from Maven central:

```
	<groupId>com.eclecticlogic</groupId>
	<artifactId>pedal</artifactId>
	<version>1.4.1</version>
```

Minimum dependencies that you need to provide in your application:

1. slf4j (over logback or log4j) v1.7.7 or higher
2. spring-tx, spring-context and spring-orm v4.0 or higher
3. JTA v 1.1
4. hibernate-core and hibernate-entitymanager 4.3 or higher.
5. JDBC4 compliant driver and connection pool manager (BoneCP recommended).
6. For data loader: groovy-all 2.3 or above

# Configuration

## Setting up Spring beans 

Pedal's transaction object requires an implementation of PlatformTransactionManager which Spring's JPATransactionManager (and JTATransactionManager) provide. However, to enable advanced features, we recommend using Pedal's JPATransactionWrapper (which derives from JPATransactionManager). After wiring up your usual suspect JPA/Hibernate Spring beans, create and instance of Pedal's TransactionImpl and set the platformTransactionManager property to the JPATransactionWrapper bean reference (example below in the next section on DAO wiring).

## Wiring up DAOs

Pedal DAO classes should derive from the AbstractDAO base class. It is recommended that you introduce an application specific parent class that dervices from AbstractDAO and have your DAOs derive from the application specific classes. The minimum requirement for a DAO class is to provide the implementation of the abstract getEntityClass() method:

```
    public Class<E> getEntityClass();
```
This is usually as simple as (for the Entity class Student.java; the code is necessitated by the limitations of generics in Java):

```
	public class StudentDAO extends AbstractDAO<Student> { 
    	@override
	    public Class<Student> getEntityClass() {
    		return Student.class;
	    }
	}
```

The DAO should also be provided with a reference to an EntityManager. This can be done as part of Spring wiring or in code as:

```
    @Override
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
```

The StudentDAO now supports basic CRUD operations and provides a fluent API for selects.  

The typical Spring based wiring of a DAO (with an application specific parent DAO called AppDAO) looks like this:

```
	<bean id="ptf" class="com.eclecticlogic.pedal.impl.JPATransactionWrapper">
		<property name="entityManagerFactory" ref="emf" />
	</bean>

	<bean id="tx" class="com.eclecticlogic.pedal.impl.TransactionImpl">
		<property name="platformTransactionManager" ref="ptf" />
	</bean>
	
	<!-- Optional provider-api -->
	<bean id="hibernateProviderAccess" class="com.eclecticlogic.pedal.provider.hibernate.HibernateProviderAccessSpiImpl">
		<property name="entityManagerFactory" ref="emf" />
	</bean>
	
    <bean id="abstractDAO" abstract="true" class="com.myapp.AppDAO">
		<property name="transaction" ref="tx" />
		<property name="providerAccess" ref="hibernateProviderAccess" />
	</bean>

	<bean parent="abstractDAO" class="com.eclecticlogic.pedal.test.dm.dao.StudentDAO" />
``` 

# Usage

## Create, Update, Delete

To create an entity, simply call the DAO's create method. 

```
    Student student = new Student();
    // set various attributes
    studentDAO.create(student);
    // also studentDAO.create(student1, student2 ...);
```
Similarly, to update an entity call the update(entity) method and for deleting the delete(entity) method.

## Queries

We strongly recommend that all queries be contained within the DAO class. To write a select query, you can use CritieriaQuery objects, HQL, JPA-QL or native SQL. Here is a simple HQL query implementation in the StudentDAO:

```
    public List<Student> findByGradeAndGPARange(int grade, float gpaLow, float gpaHigh) {
        return select("from Student where grade = :grade and gpa between :gpaLow and :gpaHigh") //
                .bind("grade", grade) //
                .bind("gpaLow", gpaLow) //
                .bind("gpaHigh", gpaHigh) //
                .list();
    }
```

You can use the get() method to return just one result (it returns Optional<T>) or the "Optional<R> scalar()" method to get a scalar result back or the "List<R> scalarList()" method to get a scalar list back. You can also use the "returning(int maxResults)" and "startingAt(int startPosition)" methods to page through the result lists. You can also specify locking mode with the "using(LockModeType lock)" method. The select() method of the Abstract DAO has overloaded variants to accept CriteriaQuery query objects and native queries. 

## Update Queries

The update(query) api is similar to the select and can be used to execute updates and deletes. Here is an example:

```
   public int graduateAboveGPA(float gpa) {
		update("update Student set grade = grade + 1 where gpa >= :gpa") //
                .bind("gpa", gpa) //
                .update(); // returns rows updated.  
	}
```

# Transaction Management

## Transactions

The Pedal Transaction object allows programmatic transaction delineation alongside Spring's @Transactional annotation. To execute a block of code in a transaction, simply get a reference to an injected transaction reference (you can supply the transaction reference to the AbstractDAO) and call either the run method (no return value) or exec method. The run has variants that takes a Consumer or Runnable. The consumer gets a reference to a "Context" instance. The exec can take a Supplier (no Context reference) or a Function that takes a Context and returns a value. The following snipper shows the transaction in action:

```
   public void createStudentAndScore() {
       getTransaction().run(() -> {
           Student student = new Student();
           student...
           getStudentDAO.create(student);
           
           Score score = new Score();
           score.setStudent(student);
           ...
           getScoreDAO().create(score);
       });
   }
```

## Transaction attached objects

The real power of the Pedal Transaction is in its ability to attach data and jobs to the current transaction. To enable this feature, you must use Pedal's JPATransactionWrapper instead of Spring's JPATransactionManager. Here is an example of setting and retrieving transaction attached data:

```
	public void setupData() {
	    getTransaction().run(context -> {
           
           context.put("myKey", someObject);
           verifyData();           
        });
    }
    
    public void verifyData() {
        getTransaction().run(context -> {
           
            assert context.get("myKey") == someObject;
        });
    }
```
 
## Transaction attached jobs

Transaction attached jobs allow you to fire code either just before and just after the transaction commits. The after-commit is only called if the transaction successfully commits.

```
   public void jobFun() {
      getTransaction().run(context -> {
           
            context.beforeCommit((DataContext dc) -> {
                // This is called before the commit. 
            }
            
            context.afterCommit((DataContext dc) -> {
            	// The data is now in the database and visible to other transactions.
            }           
       });
   }
```

# Provider Features

Pedal currently implements support for Hibernate and allows access to the ProviderAccess interface from within the DAOs. This interface provides access to the current schema name and the table name (including overridden names in orm.xml) for entities.

# Data Loader

The pedal data loader is accessed via the Loader interface. Create an instance of LoaderImpl providing it with a reference to the dao registry. Create your load script in one or more groovy scripts in your classpath.

The Loader interface allows you to specify inputs to the script. The inputs are in the form of a map of objects to String keys. The keys will be available as variables in your script.
The loader also allows you to specify a directory (in the classpath) where all your scripts reside. All references to scripts can then be relative to that directory.

Call the load method passing in one or more scripts to load and execute. Any inputs defined are passed to all the scripts. The loader also allows you to define namespaces so that output variables from scripts are separate. A variable called x created by a script with a namespace of a can be accessed in subsequent scripts as a.x. In the returned Map, the key "a" has a map of variables, one of which is x. 

## Loader script format

The loader works using a typical groovy script with a special syntax for defining tables to load. The script can contain import statements, variable definitions, etc. Any properties defined in the script (essentially a variable without a def) will be available in the map returned by the Loader.load() method. This can be used to get specific objects or ids to test against.

### Table load definition

To insert rows into a table, use the table() method. The method takes two parameters and a closure:

Class reference of JPA entity
List of attributes you want to define.

The method returns a list of entities created in the closure.

The closure should have one more more lines of the format:

row << [<value1>, <value2>, ...]

The values are what you'd populate in the JPA entity, not in the database. So for a foreign key, you'd pass the @JoinColumn object. For a character field mapped to an Enum, you'd pass the actual Enum

Here is an example of a simple script to populate a table and then a child table:

```
	import com.test.School
    import com.test.Student
	import com.test.SchoolType
    import com.test.Gender

	table(School, [name, type, address]) {
		row 'Lee Elementary', SchoolType.ELEMENTARY, '1 Lee Rd'
		row 'Park View School', SchoolType.MIDDLE, '10 Elm Street'
		highSchool = row 'Mountain Top High', SchoolType.HIGH, '12 Dream Street'
	} 

	println highSchool.id

    table(Student, [name, gender, school]) {
		row 'Joe Schmuckately', Gender.MALE, highSchool
    }

	find(Student, 1)
```   

The above script creates three rows in the school table and also prints the database assigned id of the highSchool. The highSchool object can now be accessed from the map of variables returned by the load() method using the key "highSchool".  

Variables created in one script are available to the next script when multiple scripts are passed to the same load() call. The find method can be used to retrieve data that has been created by primary key.

# Release notes

### 1.4.17

- CopyCommand now allows access to "packaged" copy data that can be serialized elsewhere and subsequently de-serialized and inserted into the database.

### 1.4.16

- Fixed issue with CopyCommand when you have a @Transient setter without getter.

### 1.4.15

- CopyCommand synthetic extractor class is created with unique names to prevent linkage error when the command is initially called in concurrent threads.

### 1.4.14 

- Added support for embedded id pk in CopyCommand as long as @AttributeOverrides annotation is used in the Entity. Note: The CopyCommand javassist code generation logic is need of refactoring. To put it politely, it is ugly right now.

### 1.4.13

- Restrained the api set available after call to withInput

### 1.4.12

- Added withInput method to loader DSL to allow loading of another DSL with specific input values.

### 1.4.11

- Added NoopTransactionMock and NoopContext to facilitate mock testing.

### 1.4.10

- Added defaultRow closure to simply definition of default attribute values.

### 1.4.9

- Added ability to flush session to transaction and data load script.

### 1.4.8

- Added support for custom methods to be defined in the load scripts.

### 1.4.7

- Added support to specify @Column(name) via @AttributeOverrides/@AttributeOverride annotation. However, @Column annotation is still expected on the getter method.

### 1.4.6

- Added support to have empty collections be recorded as NULL using @CopyEmptyAsNull annotation for copy-command.

### 1.4.5

- Added support for copy-command to work with array types that are mapped to java.util.Collection derivatives.

### 1.4.4

- Reduced scope of provided dependencies.
- Refactored DAOLite.
- Fixed issue with AbstractUserType getting null properties.

### 1.4.3

- Bug fix: Error in parameter initialization in array type.

### 1.4.2

- Simplified List and Set user types and introduced ability to define if empty list/set should be treated as null or empty array.
- Copy command records performance stats
- Introduced DAOLite for cases where simple crud operations are to be performed.

### 1.4.1 

- Variables created in one load script are available to the next.
- Find method for load script. 
- Namespaced variables in load scripts.
- Load method for use within scripts to load other scripts.

### 1.4.0

- Added DSL for easy loading of test data.

### 1.3.11

- Added ability to query custom type fields by supplying a custom binding.

### 1.3.10

- Simplified vararg methods in DAO to make it easier to work with mock frameworks.

### 1.3.9

- Introduced Javassist based copy-command data extractor.

### 1.3.6

- Modified copy-command support to build copy string automatically.

### 1.3.5

- Bug fixes.
- Support for postgresql copy command.

### 1.3.0

- Added DateTimeAwareDAO to facilitate automatic setting of inserted-on, updated-on type fields. DAOs of entities that want automatic inserted-on/updated-on values populated in create()/update() methods should implement this interface. Methods of the interface may be overridden as necessary. TemporalType.TIMESTAMP and DATE are supported. See the ManufacturerDAO and EmployeeDAO test classes for a simple example.

### 1.2.1

- Modified lock api to work with entity or id. 

### 1.2.0

- Added support for PostgreSQL bit strings (PostgresqlBitStringUserType).
- Added tests of array (mapping to list and set) and bit string types.
- Added typical database to Java/JPA hibernate reverse-engineering setup to pom.