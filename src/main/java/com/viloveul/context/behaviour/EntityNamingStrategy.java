package com.viloveul.context.behaviour;

import lombok.NoArgsConstructor;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.io.Serializable;

@NoArgsConstructor
public class EntityNamingStrategy implements PhysicalNamingStrategy, Serializable {

    protected String schema = "public";

    protected String prefix = "tbl_";

    protected static final String DEF_SCHEMA = "schema";

    protected static final String DEF_PREFIX = "tprefix_";

    public EntityNamingStrategy(String schema, String prefix) {
        this.schema = schema;
        this.prefix = prefix;
    }

    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return identifier;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        if (identifier == null) {
            return Identifier.toIdentifier(this.schema);
        }
        String name = identifier.getText();
        if (DEF_SCHEMA.equals(name)) {
            return Identifier.toIdentifier(this.schema);
        }
        return identifier;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        String name = identifier.getText();
        if (name.startsWith(DEF_PREFIX)) {
            return Identifier.toIdentifier(
                name.replace(
                    DEF_PREFIX,
                    this.prefix
                )
            );
        }
        return identifier;
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return identifier;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return identifier;
    }
}
