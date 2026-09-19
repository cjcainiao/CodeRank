package ${package.Entity};

<#list table.importPackages as pkg>
import ${pkg};
</#list>
<#-- 强制导入 @TableName 注解，避免表名与类名一致时漏 import；若自动导入已含则跳过 -->
<#assign hasTableNameImport = table.importPackages?seq_contains("com.baomidou.mybatisplus.annotation.TableName") />
<#if !hasTableNameImport>
import com.baomidou.mybatisplus.annotation.TableName;
</#if>
<#if entityLombokModel>
import lombok.Builder;
import lombok.Data;
</#if>

/**
 * <p>
 * ${table.comment!}
 * </p>
 *
 * @author ${author}
 */
<#if entityLombokModel>
@Data
@Builder
</#if>
@TableName("${table.name}")
<#if superEntityClass??>
public class ${entity} extends ${superEntityClass}<#if activeRecord><${entity}></#if> {
<#elseif activeRecord>
public class ${entity} extends Model<${entity}> {
<#else>
public class ${entity} implements Serializable {
</#if>

<#if entitySerialVersionUID>
    private static final long serialVersionUID = 1L;
</#if>
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if field.keyFlag>
        <#assign keyPropertyName="${field.propertyName}"/>
    </#if>

    <#if field.comment!?length gt 0>
    /**
     * ${field.comment}
     */
    </#if>
    <#if field.keyFlag>
        <#if field.keyIdentityFlag>
    @TableId(value = "${field.annotationColumnName}", type = IdType.AUTO)
        <#elseif idType??>
    @TableId(value = "${field.annotationColumnName}", type = IdType.${idType})
        <#else>
    @TableId("${field.annotationColumnName}")
        </#if>
    <#elseif field.convert>
    @TableField("${field.annotationColumnName}")
    </#if>
    private ${field.propertyType} ${field.propertyName};
</#list>
<#------------  END 字段循环遍历  ---------->

<#if activeRecord>
    @Override
    public Serializable pkVal() {
        <#if keyPropertyName??>
        return this.${keyPropertyName};
        <#else>
        return null;
        </#if>
    }
</#if>
}
