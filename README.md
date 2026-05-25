# React.js with Spring OAuth Integration Demo

This repository demonstrates how to integrate React.js with
a Spring OAuth 2.0 Client and a Spring OAuth 2.0 Resource Server
via an API Gateway in a Backend for Frontend (BFF) pattern.

This demo was inspired by the article
"[OAuth2 Backend for Frontend With Spring Cloud Gateway](https://www.baeldung.com/spring-cloud-gateway-bff-oauth2)"
by [Jérôme Wacongne](https://www.baeldung.com/author/jeromewacongne)
(also known as [ch4mp](https://stackoverflow.com/users/619830/ch4mp)
on StackOverflow). In the article, he explores how to integrate React.js
and Spring in a BFF configuration. He configures five applications:
a Spring application configured as an OAuth 2.0 Client
and API gateway, a Spring application configured as an OAuth 2.0 resource
server, and three single-page applications (SPAs) using Angular, React,
and Vue, respectively. The rationale for using the pattern is that it
allows you to securely keep the tokens issued by your authorization
server in a backend session store.

The usual recommendation when using a React frontend in an OAuth 2.0
login flow is to configure your React frontend as an OAuth 2.0 Public
Client. This recommendation makes sense because React frontends typically
have no access to a secure backend environment. The rise of BFF
frameworks has changed this. In recent years, you can now write a React
application where most of your code lives on the frontend, but if a secure
backend environment becomes necessary, frameworks like Next.js and Nuxt
give you access to these through [API routes](https://nuxt.com/docs/4.x/directory-structure/server)
and [Server Actions](https://nextjs.org/docs/app/glossary#server-action).

With these frameworks, you can now register your React application as
an OAuth 2.0 Confidential Client. For most of your application, you can
write React code for the browser, but to handle the OAuth 2.0
Authorization Code flow, you can delegate to the API routes provided by
the BFF framework. Moreover, these API routes can securely store the
tokens you receive from the authorization server in a backend store,
like Redis or PostgreSQL, and all you have to send to the browser is an
opaque session ID.

Spring does not provide any recommendations on how you can achieve the same
pattern with applications using React for the frontend and Spring on the
backend. I believe this is why the article written by ch4mp had to be made.

One shortcoming I see with ch4mp's implementation is that his tutorial
prescribes the use of third-party starter libraries. I see no issue with
using these libraries on your own projects, but for the purpose of simplicity,
it would be nice if we had an implementation that only used the default
starter libraries provided by Spring. That is the purpose of this repository.

## License

This project is licensed under the MIT License.

Copyright © 2026 Angelo Geulin
