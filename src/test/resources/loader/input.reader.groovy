import com.eclecticlogic.pedal.forward.dm.SimpleType

table(SimpleType, ['amount']) {
    inputReaderReturn = simple1 = row (1000 * index)
    simple2 = row 2000
}