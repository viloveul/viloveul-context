package com.viloveul.context.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;
import java.util.Date;

@Setter
@Getter
@MappedSuperclass
public abstract class AbstractMidEntity extends AbstractEntity {

    @Column(name = "updated_at", insertable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date updatedAt;

    @Column(name = "updated_by", insertable = false)
    protected String updatedBy;

    @PreUpdate
    public void onBeforeUpdate() {
        this.updatedAt = new Date();
        this.updatedBy = this.getIncludeAuthoredBy();
    }

}
