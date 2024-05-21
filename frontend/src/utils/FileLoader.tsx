import React, { PureComponent, ReactElement, DragEvent } from 'react';

interface FileLoaderProps {
    onDrop?: (e: DragEvent<HTMLDivElement>) => void;
    onDragEnter?: (e: DragEvent<HTMLDivElement>) => void;
    onDragOver?: (e: DragEvent<HTMLDivElement>) => void;
    onDragLeave?: (e: DragEvent<HTMLDivElement>) => void;
    children: ReactElement;
}

class FileLoader extends PureComponent<FileLoaderProps> {
    private dragTargetSaved: EventTarget | null = null;

    constructor(props: FileLoaderProps) {
        super(props);
        this.handleDragEnter = this.handleDragEnter.bind(this);
        this.handleDrop = this.handleDrop.bind(this);
        this.handleDragOver = this.handleDragOver.bind(this);
        this.handleDragLeave = this.handleDragLeave.bind(this);
    }

    handleDrop(e: DragEvent<HTMLDivElement>) {
        e.preventDefault();
        e.stopPropagation();
        this.props.onDrop?.(e);
    }

    handleDragEnter(e: DragEvent<HTMLDivElement>) {
        e.preventDefault();
        e.stopPropagation();
        this.dragTargetSaved = e.target;
        this.props.onDragEnter?.(e);
    }

    handleDragOver(e: DragEvent<HTMLDivElement>) {
        if (!e.dataTransfer.types.includes('Files')) {
            return;
        }
        e.preventDefault();
        e.stopPropagation();
        this.props.onDragOver?.(e);
    }

    handleDragLeave(e: DragEvent<HTMLDivElement>) {
        e.preventDefault();
        e.stopPropagation();
        if (this.props.onDragLeave && this.dragTargetSaved === e.target) {
            this.props.onDragLeave(e);
        }
    }

    render() {
        let child = React.Children.only(this.props.children);
        return React.cloneElement(child, {
            onDragEnter: this.handleDragEnter,
            onDragOver: this.handleDragOver,
            onDragLeave: this.handleDragLeave,
            onDrop: this.handleDrop
        });
    }
}

export default FileLoader;
