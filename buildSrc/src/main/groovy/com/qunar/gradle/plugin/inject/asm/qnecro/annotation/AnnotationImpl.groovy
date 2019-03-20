package com.qunar.gradle.plugin.inject.asm.qnecro.annotation

import groovyjarjarasm.asm.AnnotationVisitor

import static groovyjarjarasm.asm.Opcodes.ASM5
/**
 * Created by jingmin.xing on 2015/12/14.
 */
class AnnotationImpl extends AnnotationVisitor {

    private Map<String, Object> attributes

    AnnotationImpl(int api, AnnotationVisitor annotationVisitor) {
        super(api, annotationVisitor)
    }

    @Override
    void visit(String name, Object value) {
        super.visit(name, value)
        if (attributes == null) {
            attributes = new HashMap();
        }
        attributes.put(name, value);
    }

    @Override
    void visitEnum(String name, String desc, String value) {
        super.visitEnum(name, desc, value)
        if (attributes == null) {
            attributes = new HashMap();
        }
        attributes.put(name, value);
    }

    @Override
    AnnotationVisitor visitArray(String name) {
        return new ArrayVisitor(super.visitArray(name), name);
    }

    public Map<String, Object> getAttributes() {
        return this.attributes == null ? Collections.emptyMap() : this.attributes
    }

    private final class ArrayVisitor extends AnnotationVisitor {

        private String name
        private final ArrayList<Object> values = new ArrayList()

        ArrayVisitor(AnnotationVisitor annotationVisitor, String name) {
            super(ASM5, annotationVisitor)
            this.name = name
        }

        @Override
        void visit(String name, Object value) {
            super.visit(name, value)
            this.values.add(value)
        }

        @Override
        void visitEnd() {
            super.visitEnd()
            AnnotationImpl.this.visit(this.name, this.values.toArray(new String[0]))
        }
    }
}
