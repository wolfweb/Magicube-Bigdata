package com.magicube.eventflows.Repository.Squeryl

trait NativeQueryAdapter {
  def printSchemaSql = {
    "select schema_name, schema_owner, is_default from information_schema.schemata order by schema_name;"
  }
}

case object UnKnoneAdapter extends NativeQueryAdapter {}

case object MySqlDBAdapter extends NativeQueryAdapter {}

case object H2DBAdapter extends NativeQueryAdapter {}

case object MSSqlAdapter extends NativeQueryAdapter {}
