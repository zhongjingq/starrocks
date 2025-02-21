// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.starrocks.sql.optimizer.operator.physical;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.starrocks.catalog.TableFunction;
import com.starrocks.sql.optimizer.OptExpression;
import com.starrocks.sql.optimizer.OptExpressionVisitor;
import com.starrocks.sql.optimizer.operator.OperatorType;
import com.starrocks.sql.optimizer.operator.OperatorVisitor;
import com.starrocks.sql.optimizer.operator.Projection;
import com.starrocks.sql.optimizer.operator.scalar.ColumnRefOperator;
import com.starrocks.sql.optimizer.operator.scalar.ScalarOperator;

import java.util.List;

public class PhysicalTableFunctionOperator extends PhysicalOperator {
    private final TableFunction fn;

    // Table function own output cols. It's used to validate plan.
    private final List<ColumnRefOperator> fnResultColRefs;

    // External column ref of the join logic generated by the table function
    private final List<ColumnRefOperator> outerColRefs;

    // table function input parameters.
    private final List<ColumnRefOperator> fnParamColumnRefs;

    public PhysicalTableFunctionOperator(List<ColumnRefOperator> fnResultColRefs, TableFunction fn,
                                         List<ColumnRefOperator> fnParamColumnRefs,
                                         List<ColumnRefOperator> outerColRefs,
                                         long limit,
                                         ScalarOperator predicate,
                                         Projection projection) {
        super(OperatorType.PHYSICAL_TABLE_FUNCTION);
        this.fnResultColRefs = fnResultColRefs;
        this.fn = fn;
        this.fnParamColumnRefs = fnParamColumnRefs;
        this.outerColRefs = outerColRefs;
        this.limit = limit;
        this.predicate = predicate;
        this.projection = projection;
    }

    public List<ColumnRefOperator> getFnResultColRefs() {
        return fnResultColRefs;
    }

    public TableFunction getFn() {
        return fn;
    }

    public List<ColumnRefOperator> getFnParamColumnRefs() {
        return fnParamColumnRefs;
    }

    public List<ColumnRefOperator> getOuterColRefs() {
        return outerColRefs;
    }

    // Table function node combines its child output cols and its own output cols
    public List<ColumnRefOperator> getOutputColRefs() {
        List<ColumnRefOperator> outputCols = Lists.newArrayList();
        outputCols.addAll(outerColRefs);
        outputCols.addAll(fnResultColRefs);
        return outputCols;
    }

    @Override
    public <R, C> R accept(OperatorVisitor<R, C> visitor, C context) {
        return visitor.visitPhysicalTableFunction(this, context);
    }

    @Override
    public <R, C> R accept(OptExpressionVisitor<R, C> visitor, OptExpression optExpression, C context) {
        return visitor.visitPhysicalTableFunction(optExpression, context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!super.equals(o)) {
            return false;
        }

        PhysicalTableFunctionOperator that = (PhysicalTableFunctionOperator) o;
        return Objects.equal(fn, that.fn) && Objects.equal(fnResultColRefs, that.fnResultColRefs) &&
                Objects.equal(outerColRefs, that.outerColRefs) &&
                Objects.equal(fnParamColumnRefs, that.fnParamColumnRefs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), fn, fnResultColRefs);
    }
}