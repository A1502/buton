package third.part.entity.generic;

public class BaseGeneric<T> {

//public class BaseGeneric {

    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private Long longValue;

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

   /* private long priLongValue;

    public long getPriLongValue() {
        return priLongValue;
    }

    public void setPriLongValue(long priLongValue) {
        this.priLongValue = priLongValue;
    }*/
}
