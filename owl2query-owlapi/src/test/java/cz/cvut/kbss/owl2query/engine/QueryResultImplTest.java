package cz.cvut.kbss.owl2query.engine;

import cz.cvut.kbss.owl2query.model.ResultBinding;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.when;

public class QueryResultImplTest {

    @Mock
    private InternalQuery<Object> queryMock;

    private QueryResultImpl<Object> sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(queryMock.getResultVars()).thenReturn(Collections.emptyList());
        when(queryMock.isDistinct()).thenReturn(false);
        this.sut = new QueryResultImpl<>(queryMock);
    }

    @Test
    public void addAddsBindingIntoResultBindings() {
        when(queryMock.getLimit()).thenReturn(Integer.MAX_VALUE);
        final ResultBinding<Object> rb = new ResultBindingImpl<>();
        sut.add(rb);
        assertEquals(1, sut.size());
        assertEquals(rb, sut.iterator().next());
    }

    @Test
    public void addSkipsItemWhenQueryOffsetHasNotBeenReached() {
        when(queryMock.getLimit()).thenReturn(Integer.MAX_VALUE);
        when(queryMock.getOffset()).thenReturn(2);
        final ResultBinding<Object> rb = new ResultBindingImpl<>();
        sut.add(rb);
        assertTrue(sut.isEmpty());
    }

    @Test
    public void addRejectsItemAndReturnsFalseWhenQueryLimitHasBeenReached() {
        final int limit = 2;
        when(queryMock.getLimit()).thenReturn(limit);
        final ResultBinding<Object> rbOne = new ResultBindingImpl<>();
        assertTrue(sut.add(rbOne));
        final ResultBinding<Object> rbTwo = new ResultBindingImpl<>();
        assertTrue(sut.add(rbTwo));
        final ResultBinding<Object> rbThree = new ResultBindingImpl<>();
        assertFalse(sut.add(rbThree));
        assertEquals(2, sut.size());
        final Iterator<ResultBinding<Object>> it = sut.iterator();
        assertEquals(rbOne, it.next());
        assertEquals(rbTwo, it.next());
    }

    @Test
    public void addAcceptsItemsWithinOffsetAndLimitRange() {
        final int limit = 3;
        final int offset = 2;
        when(queryMock.getLimit()).thenReturn(limit);
        when(queryMock.getOffset()).thenReturn(offset);
        final List<ResultBinding<Object>> bindings = IntStream.range(0, 10).mapToObj(i -> new ResultBindingImpl<>())
                                                              .collect(Collectors.toList());
        int i = 0;
        for (; i < limit + offset; i++) {
            assertTrue(sut.add(bindings.get(i)));
        }
        for (; i < bindings.size(); i++) {
            assertFalse(sut.add(bindings.get(i)));
        }
        assertEquals(limit, sut.size());
        final List<ResultBinding<Object>> expected = bindings.subList(offset, limit);
        final Iterator<ResultBinding<Object>> itExp = expected.iterator();
        final Iterator<ResultBinding<Object>> itAct = expected.iterator();
        while (itExp.hasNext() && itAct.hasNext()) {
            assertEquals(itExp.next(), itAct.next());
        }
    }
}
