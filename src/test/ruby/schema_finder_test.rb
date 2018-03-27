# require 'java'
require './src/main/ruby/ruby_schema_finder'
require 'minitest/autorun'

# Simple interop test
class SchemaFinderTest

  describe "Find schema" do
    before do
      @rubyFinder = RubySchema::Finder.new
      @kotlinFinder = com.looker.sql_query_parser.parser.SchemaFinder.new @rubyFinder
      colUserID = RubySchema::Column.new "table.user_id"
      colName = RubySchema::Column.new "table.name"
      @rubyFinder.columns["table.user_id"] = colUserID
      @rubyFinder.columns["table.name"] = colName
    end

    it "must find existing columns" do
      column = @kotlinFinder.findColumn "table.user_id"
      column.getName.must_equal "user_id"
      column.getTableName.must_equal "table"
      column.fullName.must_equal "table.user_id"
      column.getOriginalName.must_equal "table.user_id"
      column.name.must_equal "user_id"
      column.tableName.must_equal "table"
      column.fullName.must_equal "table.user_id"
      column.originalName.must_equal "table.user_id"

      column = @kotlinFinder.findColumn "table.name"
      column.getName.must_equal "name"
      column.getTableName.must_equal "table"
      column.fullName.must_equal "table.name"
      column.getOriginalName.must_equal "table.name"
      column.name.must_equal "name"
      column.tableName.must_equal "table"
      column.fullName.must_equal "table.name"
      column.originalName.must_equal "table.name"

      column = @rubyFinder.columns["table.user_id"]
      column.getName.must_equal "user_id"
      column.getTableName.must_equal "table"
      column.fullName.must_equal "table.user_id"
      column.getOriginalName.must_equal "table.user_id"
      column.name.must_equal "user_id"
      column.tableName.must_equal "table"
      column.fullName.must_equal "table.user_id"
      column.originalName.must_equal "table.user_id"

      column = @rubyFinder.columns["table.name"]
      column.getName.must_equal "name"
      column.getTableName.must_equal "table"
      column.fullName.must_equal "table.name"
      column.getOriginalName.must_equal "table.name"
      column.name.must_equal "name"
      column.tableName.must_equal "table"
      column.fullName.must_equal "table.name"
      column.originalName.must_equal "table.name"

    end

    it "must not find missing columns" do
      column = @kotlinFinder.findColumn "table.missing"
      assert_nil(column, "table.missing should return nil")

    end
  end

  describe "Create via Kotlin" do
    it "must create via reflection" do
      finder = com.looker.sql_query_parser.parser.RubyScript.InitObject "RubySchema$$Finder_1310805313"
      assert finder != nil
    end

    it "must create via script" do
      @rubyFinder = com.looker.sql_query_parser.parser.RubyScript.RubyObject "RubySchema::Finder.new"
      assert @rubyFinder != nil
      @kotlinFinder = com.looker.sql_query_parser.parser.SchemaFinder.new @rubyFinder
      colUserID = com.looker.sql_query_parser.parser.RubyScript.RubyObject 'RubySchema::Column.new "table.user_id"'
      colName = com.looker.sql_query_parser.parser.RubyScript.RubyObject 'RubySchema::Column.new "table.name"'
      @rubyFinder.columns["table.user_id"] = colUserID
      @rubyFinder.columns["table.name"] = colName

      column = @kotlinFinder.findColumn "table.user_id"
      column.getName.must_equal "user_id"
      column.getTableName.must_equal "table"
      column.fullName.must_equal "table.user_id"
      column.getOriginalName.must_equal "table.user_id"
      column.name.must_equal "user_id"
      column.tableName.must_equal "table"
      column.fullName.must_equal "table.user_id"
      column.originalName.must_equal "table.user_id"

      column = @kotlinFinder.findColumn "table.name"
      column.getName.must_equal "name"
      column.getTableName.must_equal "table"
      column.fullName.must_equal "table.name"
      column.getOriginalName.must_equal "table.name"
      column.name.must_equal "name"
      column.tableName.must_equal "table"
      column.fullName.must_equal "table.name"
      column.originalName.must_equal "table.name"

      column = @rubyFinder.columns["table.user_id"]
      column.getName.must_equal "user_id"
      column.getTableName.must_equal "table"
      column.fullName.must_equal "table.user_id"
      column.getOriginalName.must_equal "table.user_id"
      column.name.must_equal "user_id"
      column.tableName.must_equal "table"
      column.fullName.must_equal "table.user_id"
      column.originalName.must_equal "table.user_id"

      column = @rubyFinder.columns["table.name"]
      column.getName.must_equal "name"
      column.getTableName.must_equal "table"
      column.fullName.must_equal "table.name"
      column.getOriginalName.must_equal "table.name"
      column.name.must_equal "name"
      column.tableName.must_equal "table"
      column.fullName.must_equal "table.name"
      column.originalName.must_equal "table.name"

    end
  end

end