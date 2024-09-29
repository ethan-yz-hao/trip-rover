import {useState, useEffect} from 'react';

function useResizeObserver(element: HTMLElement | null): DOMRectReadOnly | null {
    const [dimensions, setDimensions] = useState<DOMRectReadOnly | null>(null);

    useEffect(() => {
        if (!element) {
            setDimensions(null);
            return;
        }

        const resizeObserver = new ResizeObserver(entries => {
            entries.forEach(entry => {
                setDimensions(entry.contentRect);
            });
        });

        resizeObserver.observe(element);

        return () => {
            resizeObserver.unobserve(element);
            setDimensions(null)
        };

    }, [element]);

    return dimensions;
}

export default useResizeObserver;