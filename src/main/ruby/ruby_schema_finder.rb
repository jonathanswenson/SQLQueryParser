require 'java'

java_import com.looker.sql_query_parser.parser.SchemaFinder

# schema definition for interop with Kotlin
module RubySchema
  # Implementation of ISchemaColumn
  class Column
    include com.looker.sql_query_parser.parser.ISchemaColumn
    attr_accessor :database, :tableName, :name, :originalName, :aliases, :is_id
    @database = ""
    @tableName = ""
    def initialize(originalName, aliases = [], is_id = false)
      @originalName = originalName
      @aliases = aliases
      @is_id = is_id
      parts = originalName.split(".")
      if parts.size == 1
        @name = originalName
      elsif parts.size == 2
        @tableName = parts[0]
        @name = parts[1]
      elsif parts.size == 3
        @database = parts[0]
        @tableName = parts[1]
        @name = parts[2]
      end
    end

    def fullName
      if @database
        "#{@database}.#{@tableName}.#{@name}"
      elsif @tableName
        "#{@tableName}.#{@name}"
      else
        "#{@name}"
      end
    end

    def getOriginalName
      @originalName
    end

    def setOriginalName(s)
      @originalName = s
    end

    def getAliases
      @aliases
    end

    def setAliases(aliases)
      @aliases = aliases
    end

    def is_id
      @is_id
    end

    def set_id(b)
      @is_id = b
    end

    def getDatabase
      @database
    end

    def setDatabase(s)
      @database = s
    end

    def getName
      @name
    end

    def setName(s)
      @name = s
    end

    def getTableName
      @tableName
    end

    def setTableName(s)
      @tableName = s
    end
  end

  # Ruby schema finder locates columns and tables by name and returns interface ref
  class Finder
    include com.looker.sql_query_parser.parser.ISchemaFinder


    def initialize
      @tables = {}
      @columns = {}
    end

    def columns
      @columns
    end

    def tables
      @tables
    end

    # def get_column(name)
    #   @columns[name]
    # end
    def getColumn(name)
      @columns[name]
    end

    def getTable(name)
      @tables[name]
    end

    def addColumn(column)
      @columns[column.fullName()] = column
      column
    end

    def addTable(table)
      @tables[table.name] = table
      table
    end

  end

end