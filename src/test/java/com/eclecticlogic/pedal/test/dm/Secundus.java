package com.eclecticlogic.pedal.test.dm;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@SuppressWarnings("serial")
@Entity
@Table(name = "SECUNDUS")
public class Secundus implements Serializable {

    private long id;
    private String name;
    private Primus primus;


    @GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "primus"))
    @Id
    @GeneratedValue(generator = "generator")
    @Column(name = "primus", unique = true, nullable = false)
    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    public Primus getPrimus() {
        return primus;
    }


    public void setPrimus(Primus primus) {
        this.primus = primus;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

}
