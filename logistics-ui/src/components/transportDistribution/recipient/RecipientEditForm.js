import * as React from 'react';
import {Field, Form, Formik} from 'formik';
import {TextField} from 'formik-material-ui';
import Dashboard from "../../../pages/transportDistribution/Dashboard";
import {Recipient} from "../../../types";
import type {Values} from "./RecipientAddForm";
import update from "immutability-helper";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import Button from "@material-ui/core/Button/Button";
import Grid from "@material-ui/core/Grid/Grid";
import withStyles from "@material-ui/core/styles/withStyles";

type Props = {
    recipient: Recipient,
    dashboardRef: Dashboard
}

export function RecipientEditForm(props: Props) {
    return <Formik
        initialValues={{
            name: props.recipient.name,
            demand: props.recipient.demand,
            available: props.recipient.available || '',
            priority: props.recipient.priority ||'',
            limit: props.recipient.limit || '',
            saleProfit: props.recipient.saleProfit || ''
        }}
        validate={values => {
            const errors: Partial<Values> = {};
            if (!values.name) {
                errors.name = 'Required';
            }
            if (!values.demand) {
                errors.demand = 'Required'
            }else{
                if(values.demand <= 0){
                    errors.demand = 'Demand must be positive value'
                }
            }
            return errors;
        }}
        onSubmit={(values, {setSubmitting}) => {
            setSubmitting(false);
            props.dashboardRef.handleUpdateRecipient(
                update(props.recipient, {
                    name: {$set: values.name || props.recipient.name},
                    available: {$set: Math.min(values.available || props.recipient.available, values.demand || props.recipient.demand)},
                    priority: {$set: values.priority || props.recipient.priority},
                    limit: {$set: values.limit || props.recipient.limit},
                    demand: {$set: values.demand || props.recipient.demand},
                    saleProfit: {$set: values.saleProfit || props.recipient.saleProfit}
                })
            )
        }}
        render={({submitForm, isSubmitting}) => (
            <Form>
                <Grid
                    container
                    justify="center"
                    alignItems="center"
                    spacing={24}
                >
                    <Grid item xs>
                        <Field
                            name="name"
                            type="text"
                            label="Recipient name"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Field
                            name="demand"
                            type="number"
                            label="Maximal demand"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Field
                            name="available"
                            type="number"
                            label="Currently available"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Field
                            name="limit"
                            type="number"
                            label="Disposal limit"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Field
                            name="priority"
                            type="number"
                            label="Recipient priority"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Field
                            name="saleProfit"
                            type="number"
                            label="Unit sale profit"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Button
                            variant="contained"
                            color="primary"
                            disabled={isSubmitting}
                            onClick={submitForm}
                        >
                            <FontAwesomeIcon icon={"save"}/>
                        </Button>
                    </Grid>
                </Grid>
            </Form>
        )}
    />
}

const styles = theme => ({
    root:{
        padding: theme.spacing.unit * 2
    }
});

export default withStyles(styles)(RecipientEditForm);

