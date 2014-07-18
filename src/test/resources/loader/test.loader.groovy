import com.eclecticlogic.pedal.forward.dm.ExoticTypes
import com.eclecticlogic.pedal.forward.dm.Status;


def name = 'pedal'

def i = 0;
3.times {
    def tone = table (ExoticTypes, ['login', 'countries', 'authorizations', 'scores', 'status']) {
        row << ["${name}_${i++}", [true, false, true, false, false, false, false], ['create', 'update'], [1, 2, 3, 6, 10], Status.ACTIVE]
    }
}

