import React, { useEffect, useState } from 'react';
import keycloak from './keycloak';
import ReportPage from './components/ReportPage';

const App: React.FC = () => {
    const [authenticated, setAuthenticated] = useState(false);
    const [initializing, setInitializing] = useState(true);

    useEffect(() => {
        keycloak
            .init({
                onLoad: 'login-required',
                pkceMethod: 'S256',
            })
            .then((auth) => {
                setAuthenticated(auth);
            })
            .catch((error) => {
                console.error('Keycloak initialization failed', error);
            })
            .finally(() => {
                setInitializing(false);
            });
    }, []);

    if (initializing) {
        return <div>Authenticating...</div>;
    }

    if (!authenticated) {
        return <div>Authentication failed</div>;
    }

    return <ReportPage />;
};

export default App;