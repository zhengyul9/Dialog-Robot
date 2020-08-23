package ai.hual.labrador.nlu;

import javax.annotation.Nonnull;

public class QueryActDistinctWrapper implements Comparable<QueryActDistinctWrapper> {

    private QueryAct queryAct;

    QueryActDistinctWrapper(@Nonnull QueryAct queryAct) {
        this.queryAct = queryAct;
    }

    public QueryAct getQueryAct() {
        return queryAct;
    }

    @Override
    public int hashCode() {
        return queryAct.getPQuery().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryActDistinctWrapper)) {
            return false;
        }
        return queryAct.getPQuery().equals(((QueryActDistinctWrapper) o).queryAct.getPQuery());
    }

    @Override
    public int compareTo(@Nonnull QueryActDistinctWrapper o) {
        return Double.compare(o.queryAct.getScore(), queryAct.getScore());
    }
}
