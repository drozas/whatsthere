package es.whatsthere.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Result {

    @SerializedName("class")
    @Expose
    private String _class;
    @SerializedName("p")
    @Expose
    private Double p;

    public String getClass_() {
        return _class;
    }

    public void setClass_(String _class) {
        this._class = _class;
    }

    public Double getP() {
        return p;
    }

    public void setP(Double p) {
        this.p = p;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("_class", _class).append("p", p).toString();
    }
}