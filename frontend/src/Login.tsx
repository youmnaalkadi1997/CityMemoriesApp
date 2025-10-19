function login() {
    window.open('/oauth2/authorization/github', '_self');
}
export default function Login() {

    return(
            <div className="container">
                <button onClick={login}>Login With Github</button>
            </div>
    )
}