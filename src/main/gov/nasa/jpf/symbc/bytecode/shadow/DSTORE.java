/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package gov.nasa.jpf.symbc.bytecode.shadow;

import gov.nasa.jpf.vm.bytecode.StoreInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMInstructionVisitor;
import gov.nasa.jpf.jvm.bytecode.JVMLocalVariableInstruction;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Store double into local variable ..., value => ...
 */
public class DSTORE extends JVMLocalVariableInstruction implements StoreInstruction {

    public DSTORE(int localVarIndex) {
        super(localVarIndex);
    }

    @Override
    public Instruction execute(ThreadInfo ti) {
        StackFrame frame = ti.getModifiableTopFrame();

        /* jpf-shadow start modification */
        // pass instruction to the stackframe in order for it to handle diffExpressions
        frame.storeLongOperand(index, ti, this);
        /* jpf-shadow end modification */

        return getNext(ti);
    }

    @Override
    public int getLength() {
        return 2; // opcode, index
    }

    @Override
    public int getByteCode() {
        switch (index) {
        case 0:
            return 0x47;
        case 1:
            return 0x48;
        case 2:
            return 0x49;
        case 3:
            return 0x4a;
        }

        return 0x39; // ?? wide
    }

    @Override
    public String getBaseMnemonic() {
        return "dstore";
    }

    @Override
    public void accept(JVMInstructionVisitor insVisitor) {
        insVisitor.visit(this);
    }
}
