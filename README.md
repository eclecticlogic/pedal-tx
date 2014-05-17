Pedal
=====

A Java 8 based idiomatic JPA DAO framework. Let the examples say the rest.

# Getting started

Download the Pedal jar from Maven central:

```
	<groupId>com.eclecticlogic</groupId>
	<artifactId>pedal</artifactId>
	<version>1.0.0</version>
```

Minimum dependencies that you need to provide in your application:

1. slf4j (over logback or log4j) v1.7.7 or higher
2. spring-tx, spring-context and spring-orm v4.0 or higher
3. JTA v 1.1
4. hibernate-core and hibernate-entitymanager 4.3 or higher.
5. JDBC4 compliant driver and connection pool manager (BoneCP recommended).

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