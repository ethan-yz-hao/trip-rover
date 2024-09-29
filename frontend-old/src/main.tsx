import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'
import {ChakraProvider, ColorModeScript} from '@chakra-ui/react'
import theme from './theme'
import {APIProvider} from '@vis.gl/react-google-maps';
import {store} from './store'
import {Provider as ReducxProvider} from 'react-redux'

console.log(import.meta.env.VITE_GOOGLE_MAPS_API_KEY!)

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <ReducxProvider store={store}>
            <APIProvider
                apiKey={import.meta.env.VITE_GOOGLE_MAPS_API_KEY!}
                version={'beta'}
                region='US'
                language='en'
            >
                <ChakraProvider theme={theme}>
                    <ColorModeScript initialColorMode={theme.config.initialColorMode}/>
                    <App/>
                </ChakraProvider>
            </APIProvider>
        </ReducxProvider>
    </React.StrictMode>,
)
