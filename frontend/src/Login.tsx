export default function Login() {


    function login() {
        window.open('/oauth2/authorization/github', '_self');
    }

    return(
        <>
            <div className="container">
                <button onClick={login}>Login With Github</button>
            </div>
        </>
    )
}