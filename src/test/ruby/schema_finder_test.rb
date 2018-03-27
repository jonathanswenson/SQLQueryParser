require 'java'
require 'minitest/autorun'

java_import com.looker.sql_query_parser.parser.SchemaFinder

class SchemaColumn
  include com.looker.sql_query_parser.parser.ISchemaColumn
  # attr_accessor :database, :tableName, :name, :originalName, :aliases, :is_id
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

class RubySchemaFinder
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

  def getColumn(name)
    @columns[name]
  end

  def getTable(name)
    @tables[name]
  end

end

# Simple interop test
class SchemaFinderTest

  describe "Find schema" do
    before do
      @rubyFinder = RubySchemaFinder.new
      @kotlinFinder = com.looker.sql_query_parser.parser.SchemaFinder.new @rubyFinder
      colUserID = SchemaColumn.new "table.user_id"
      colName = SchemaColumn.new "table.name"
      @rubyFinder.columns["table.user_id"] = colUserID
      @rubyFinder.columns["table.name"] = colName
    end

    it "must find existing columns" do
      column = @kotlinFinder.findColumn "table.user_id"
      column.getName.must_equal "user_id"
      column.getTableName.must_equal "table"
      column.fullName.must_equal "table.user_id"
      column.getOriginalName.must_equal "table.user_id"

      column = @kotlinFinder.findColumn "table.name"
      column.getName.must_equal "name"
      column.getTableName.must_equal "table"
      column.fullName.must_equal "table.name"
      column.getOriginalName.must_equal "table.name"
    end

    it "must not find missing columns" do
      column = @kotlinFinder.findColumn "table.missing"
      assert_nil(column, "table.missing should return nil")

    end
  end

end