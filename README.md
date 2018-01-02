# SQLQueryParser

## Project goals (proposal)

Use a valid `SELECT` statements from SQL Runner to create a baseline `LookML` model optimized for that `SELECT`'s exploration.

### Convert `SELECT` to `ViewSchema`

Given a `SELECT` statement, extract all view schema that can be derived from the statement. This structural information includes:

- ViewSchema
    - **Table(s)** referenced in the `SELECT`
        - **columns(s)** referenced for the table
            - **key types** for columns
                - PK
                - FK
                - `JOIN` key (derived from equality comparison)
            - reference to Database schema object
            - `SQL` for "calculated" columns that are special SQL expressions
                - if the SQL can be parsed to extract columnar reference, this should be used to reference the relevant schema object
        - reference to Database schema object
        - **`JOIN` types**
            - table name(s)
                - key column(s)
                    - relationship (1:1, 1:Many)
            - `INNER`, `OUTER`, `LEFT`, `RIGHT`
    - **Id**: hash key (based on tables, columns, and joins to use as unique view key)
    - **View(s)** collection of nested `ViewSchema` keys (based on subselects)
    - `fun hashKey() -> string`
    - `fun toLookML() -> LookML`

### Extracting `ViewSchema` from `SELECT` grammar ASTs

The AST produced from processing a SELECT statement is far more granular than we need for generating LookML. After producing the AST, we need `func CreateViewSchemaFromAST() -> ViewSchema`

The SELECT grammar must tolerate variances in SQL so dialect-specific functions and syntax can be processed without causing errors.

In producing the view, all aliases on table or field names will be resolved to fully qualified original names. Because all metadata for the connection is available when producing `ViewSchema`, this should be a simple lookup.

When determining the type of relationship between the Join keys, we can SELECT the joined columns with GROUP BY expressions to learn whether the JOIN relationship is 1:1, 1:many, or many:many.

### Using `ViewSchema`

`ViewSchema` can be used both for generating LookML and analysis of the `SELECT`.

#### Generating LookML

The same `ViewSchema` may be referenced more than once in multiple sub-selects. By tracking the `ViewSchema.ID` for the LookML per created `ViewSchema`, we can avoid infinite recursion when creating the LookML.

#### Analyzing `SELECT`s

There are a variety of recommendations we can make by looking at the ViewData extraction, such as:

- inefficient or contradictory joins
- joins without indexed fields

and so on

## John's notes

- For IntelliJ IDEA, `View | Tool Windows | Maven Projects`
- I had to select my global JDK to get maven to compile successfully

