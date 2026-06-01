package net.officefloor.web.rest.build;

/** {@link MomentoKey} implementation. */
public class MomentoKeyImpl<M> implements MomentoKey<M> {

    static int getMomentoIndex(MomentoKey<?> momentoKey) {
        return ((MomentoKeyImpl<?>) momentoKey).momentoIndex;
    }

    private final int momentoIndex;

    MomentoKeyImpl(int momentoIndex) {
        this.momentoIndex = momentoIndex;
    }
}
